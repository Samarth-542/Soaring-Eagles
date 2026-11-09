package in.soaringeagles.api.registrations;

import java.util.UUID;

import in.soaringeagles.api.events.PaymentMethod;
import in.soaringeagles.api.integrations.payment.PaymentGateway;
import in.soaringeagles.api.integrations.payment.PaymentProviderUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class);

    private final RegistrationReservationService reservationService;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentGateway paymentGateway;

    RegistrationService(
            RegistrationReservationService reservationService,
            PaymentAttemptService paymentAttemptService,
            PaymentGateway paymentGateway) {
        this.reservationService = reservationService;
        this.paymentAttemptService = paymentAttemptService;
        this.paymentGateway = paymentGateway;
    }

    public CheckoutStartResponse start(UUID eventId, UUID idempotencyKey, RegistrationRequest request) {
        RegistrationReservation reservation = reservationService.reserve(eventId, idempotencyKey, request);
        return prepareCheckout(reservation);
    }

    public CheckoutStartResponse retry(UUID registrationId) {
        RegistrationReservation reservation = reservationService.reserveRetry(registrationId);
        return prepareCheckout(reservation);
    }

    public RegistrationStatusView status(UUID registrationId) {
        return reservationService.getStatus(registrationId);
    }

    private CheckoutStartResponse prepareCheckout(RegistrationReservation reservation) {
        if (reservation.state() == RegistrationState.CONFIRMED) {
            return new CheckoutStartResponse(
                    reservation.registrationId(),
                    reservation.state(),
                    null,
                    reservation.feeInr(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    "Registration confirmed.");
        }
        if (reservation.paymentMethod() == PaymentMethod.GOOGLE_FORM) {
            return new CheckoutStartResponse(
                    reservation.registrationId(),
                    reservation.state(),
                    reservation.holdExpiresAt(),
                    reservation.feeInr(),
                    reservation.paymentAttemptId(),
                    null,
                    null,
                    null,
                    null,
                    false,
                    "Complete the Google Form payment and upload your payment screenshot to finish registration.");
        }
        if (!reservation.needsOrderCreation()) {
            return checkoutResponse(reservation, reservation.razorpayOrderId(), null);
        }

        try {
            String orderId = paymentGateway.createOrder(
                    reservation.paymentAttemptId().toString(),
                    reservation.amountPaise(),
                    "INR",
                    reservation.registrationId().toString());
            paymentAttemptService.markReady(reservation.paymentAttemptId(), orderId);
            return checkoutResponse(reservation, orderId, null);
        } catch (PaymentProviderUnavailableException exception) {
            LOGGER.warn("Razorpay checkout could not be initialized for payment attempt {}.",
                    reservation.paymentAttemptId(), exception);
            reservationService.failAttempt(reservation.paymentAttemptId());
            return new CheckoutStartResponse(
                    reservation.registrationId(),
                    RegistrationState.PAYMENT_FAILED,
                    null,
                    reservation.feeInr(),
                    reservation.paymentAttemptId(),
                    null,
                    null,
                    reservation.amountPaise(),
                    "INR",
                    true,
                    "Checkout could not be started. Retry using the same registration details.");
        }
    }

    private CheckoutStartResponse checkoutResponse(
            RegistrationReservation reservation,
            String orderId,
            String message) {
        return new CheckoutStartResponse(
                reservation.registrationId(),
                reservation.state(),
                reservation.holdExpiresAt(),
                reservation.feeInr(),
                reservation.paymentAttemptId(),
                orderId,
                paymentGateway.checkoutKeyId(),
                reservation.amountPaise(),
                "INR",
                false,
                message);
    }
}
