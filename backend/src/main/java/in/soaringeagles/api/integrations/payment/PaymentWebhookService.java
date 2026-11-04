package in.soaringeagles.api.integrations.payment;

import java.time.Clock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.soaringeagles.api.integrations.email.RegistrationEmailQueueService;
import in.soaringeagles.api.registrations.PaymentAttempt;
import in.soaringeagles.api.registrations.PaymentAttemptRepository;
import in.soaringeagles.api.registrations.PaymentAttemptState;
import in.soaringeagles.api.registrations.ProcessedPaymentWebhookEvent;
import in.soaringeagles.api.registrations.ProcessedPaymentWebhookEventRepository;
import in.soaringeagles.api.registrations.Registration;
import in.soaringeagles.api.registrations.RegistrationRepository;
import in.soaringeagles.api.registrations.RegistrationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentWebhookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentWebhookService.class);

    private final PaymentGateway paymentGateway;
    private final ProcessedPaymentWebhookEventRepository webhookEventRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationEmailQueueService emailQueueService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    PaymentWebhookService(
            PaymentGateway paymentGateway,
            ProcessedPaymentWebhookEventRepository webhookEventRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            RegistrationRepository registrationRepository,
            RegistrationEmailQueueService emailQueueService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.paymentGateway = paymentGateway;
        this.webhookEventRepository = webhookEventRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.registrationRepository = registrationRepository;
        this.emailQueueService = emailQueueService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public void process(String providerEventId, String signature, byte[] rawBody) {
        if (!paymentGateway.isWebhookConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment verification is unavailable.");
        }
        if (!paymentGateway.isWebhookSignatureValid(rawBody, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid payment webhook signature.");
        }
        if (providerEventId == null || providerEventId.isBlank() || providerEventId.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment webhook event identifier.");
        }
        if (webhookEventRepository.existsById(providerEventId)) return;

        JsonNode payload;
        try {
            payload = objectMapper.readTree(rawBody);
        } catch (java.io.IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed payment webhook payload.");
        }
        String eventName = text(payload, "event");
        JsonNode paymentEntity = payload.path("payload").path("payment").path("entity");
        if (paymentEntity.isMissingNode()) {
            record(providerEventId, "PROCESSED");
            return;
        }

        String orderId = text(paymentEntity, "order_id");
        if (orderId == null) {
            record(providerEventId, "REVIEW");
            LOGGER.warn("Signed Razorpay webhook {} did not include an order ID.", providerEventId);
            return;
        }
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderIdForUpdate(orderId).orElse(null);
        if (attempt == null) {
            record(providerEventId, "REVIEW");
            LOGGER.warn("Signed Razorpay webhook {} referenced an unknown order.", providerEventId);
            return;
        }
        Registration registration = registrationRepository.findByIdForUpdate(
                        attempt.getRegistration().getId())
                .orElseThrow(() -> new IllegalStateException("Registration for payment attempt was not found."));
        JsonNode amountNode = paymentEntity.path("amount");
        long amount = amountNode.canConvertToLong() ? amountNode.longValue() : -1;
        String currency = text(paymentEntity, "currency");
        String paymentId = text(paymentEntity, "id");
        String paymentStatus = text(paymentEntity, "status");

        if ("payment.failed".equals(eventName)) {
            if (attempt.getState() == PaymentAttemptState.CAPTURED) {
                record(providerEventId, "PROCESSED");
                return;
            }
            attempt.markFailed(clock.instant());
            PaymentAttempt latest = paymentAttemptRepository
                    .findFirstByRegistrationIdOrderByAttemptNumberDesc(registration.getId())
                    .orElse(attempt);
            if (latest.getId().equals(attempt.getId())
                    && registration.getState() == RegistrationState.PENDING_PAYMENT) {
                registration.fail(clock.instant());
            }
            record(providerEventId, "PROCESSED");
            return;
        }

        if (!("payment.captured".equals(eventName) || "order.paid".equals(eventName))
                || !"captured".equals(paymentStatus)
                || amount != attempt.getAmountPaise()
                || !"INR".equals(currency)
                || paymentId == null) {
            record(providerEventId, "REVIEW");
            LOGGER.warn("Signed Razorpay webhook {} did not match the expected payment details.", providerEventId);
            return;
        }

        if (registration.getState() == RegistrationState.CONFIRMED) {
            if (!paymentId.equals(attempt.getRazorpayPaymentId())) {
                LOGGER.error(
                        "A second captured Razorpay payment {} was received for confirmed registration {}.",
                        paymentId,
                        registration.getId());
                record(providerEventId, "REVIEW");
            } else {
                record(providerEventId, "PROCESSED");
            }
            return;
        }

        attempt.markCaptured(paymentId, clock.instant());
        boolean holdValid = registration.getState() == RegistrationState.PENDING_PAYMENT
                && registration.getHoldExpiresAt() != null
                && registration.getHoldExpiresAt().isAfter(clock.instant());
        if (holdValid) {
            registration.confirm(clock.instant());
            emailQueueService.queue(registration.getId());
            record(providerEventId, "PROCESSED");
        } else {
            registration.needsPaymentReview(clock.instant());
            record(providerEventId, "REVIEW");
            LOGGER.error(
                    "Captured Razorpay payment {} could not be matched to an active registration hold {}.",
                    paymentId,
                    registration.getId());
        }
    }

    private void record(String eventId, String state) {
        webhookEventRepository.saveAndFlush(
                new ProcessedPaymentWebhookEvent(eventId, clock.instant(), state));
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
