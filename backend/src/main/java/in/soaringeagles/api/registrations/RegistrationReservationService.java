package in.soaringeagles.api.registrations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import in.soaringeagles.api.events.Event;
import in.soaringeagles.api.events.EventRepository;
import in.soaringeagles.api.events.EventStatus;
import in.soaringeagles.api.integrations.payment.PaymentGateway;
import in.soaringeagles.api.integrations.email.RegistrationEmailQueueService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationReservationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final RegistrationCapacityService capacityService;
    private final RegistrationProperties registrationProperties;
    private final RegistrationFieldValidator registrationFieldValidator;
    private final RegistrationEmailQueueService emailQueueService;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    RegistrationReservationService(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            RegistrationCapacityService capacityService,
            RegistrationProperties registrationProperties,
            RegistrationFieldValidator registrationFieldValidator,
            RegistrationEmailQueueService emailQueueService,
            PaymentGateway paymentGateway,
            Clock clock) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.capacityService = capacityService;
        this.registrationProperties = registrationProperties;
        this.registrationFieldValidator = registrationFieldValidator;
        this.emailQueueService = emailQueueService;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    @Transactional
    public RegistrationReservation reserve(UUID eventId, UUID idempotencyKey, RegistrationRequest request) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        Instant now = clock.instant();

        var existing = registrationRepository.findByEventIdAndIdempotencyKey(eventId, idempotencyKey);
        if (existing.isPresent()) {
            return existingReservation(existing.get());
        }

        requireOpenEvent(event, now);
        registrationFieldValidator.validate(event, request);
        BigDecimal fee = event.getFeeInr();
        boolean paid = fee.signum() > 0;
        if (paid && !paymentGateway.isCheckoutConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Online registration is temporarily unavailable. Please try again later.");
        }

        if (capacityService.countCapacitySlotsInUse(eventId, now) >= event.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration capacity has been reached.");
        }

        Instant holdExpiresAt = paid ? now.plus(registrationProperties.holdDuration()) : null;
        Registration registration = new Registration(
                event,
                idempotencyKey,
                request,
                now,
                holdExpiresAt);
        if (!paid) registration.confirm(now);
        registrationRepository.saveAndFlush(registration);

        if (!paid) {
            emailQueueService.queue(registration.getId());
            return new RegistrationReservation(
                    registration.getId(), null, registration.getState(), null, fee, 0, null, false);
        }

        long amountPaise = fee.movePointRight(2).longValueExact();
        PaymentAttempt attempt = new PaymentAttempt(registration, 1, amountPaise, now);
        paymentAttemptRepository.saveAndFlush(attempt);
        return new RegistrationReservation(
                registration.getId(),
                attempt.getId(),
                registration.getState(),
                holdExpiresAt,
                fee,
                amountPaise,
                null,
                true);
    }

    @Transactional
    public RegistrationReservation reserveRetry(UUID registrationId) {
        Registration reference = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
        UUID eventId = reference.getEvent().getId();
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
        Instant now = clock.instant();

        if (registration.getState() == RegistrationState.PENDING_PAYMENT
                && registration.getHoldExpiresAt() != null
                && registration.getHoldExpiresAt().isAfter(now)) {
            PaymentAttempt current = paymentAttemptRepository
                    .findFirstByRegistrationIdOrderByAttemptNumberDesc(registrationId)
                    .orElseThrow(() -> new IllegalStateException("Pending registration has no payment attempt."));
            if (current.getState() == PaymentAttemptState.READY) {
                return reservationFor(registration, current, false);
            }
            if (current.getState() == PaymentAttemptState.CREATING) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A payment attempt is already being prepared.");
            }
        }

        if (registration.getState() == RegistrationState.PENDING_PAYMENT) {
            registration.expire(now);
        }
        if (registration.getState() != RegistrationState.PAYMENT_FAILED
                && registration.getState() != RegistrationState.EXPIRED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This registration cannot be retried.");
        }
        if (!isEventOpen(event, now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration for this event is closed.");
        }
        if (!paymentGateway.isCheckoutConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Online registration is temporarily unavailable. Please try again later.");
        }
        if (capacityService.countCapacitySlotsInUse(eventId, now) >= event.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration capacity has been reached.");
        }

        Instant expiresAt = now.plus(registrationProperties.holdDuration());
        registration.retry(now, expiresAt);
        int nextAttemptNumber = paymentAttemptRepository
                .findFirstByRegistrationIdOrderByAttemptNumberDesc(registrationId)
                .map(attempt -> attempt.getAttemptNumber() + 1)
                .orElse(1);
        long amountPaise = registration.getFeeInr().movePointRight(2).longValueExact();
        PaymentAttempt attempt = new PaymentAttempt(registration, nextAttemptNumber, amountPaise, now);
        paymentAttemptRepository.saveAndFlush(attempt);
        return new RegistrationReservation(
                registration.getId(),
                attempt.getId(),
                registration.getState(),
                expiresAt,
                registration.getFeeInr(),
                amountPaise,
                null,
                true);
    }

    @Transactional(readOnly = true)
    public RegistrationStatusView getStatus(UUID registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
        RegistrationState state = registration.getState();
        Instant expiry = registration.getHoldExpiresAt();
        if (state == RegistrationState.PENDING_PAYMENT && expiry != null && !expiry.isAfter(clock.instant())) {
            state = RegistrationState.EXPIRED;
            expiry = null;
        }
        return new RegistrationStatusView(
                state,
                expiry,
                state == RegistrationState.PAYMENT_FAILED || state == RegistrationState.EXPIRED);
    }

    @Transactional
    public void failAttempt(UUID attemptId) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalStateException("Payment attempt disappeared."));
        if (attempt.getState() == PaymentAttemptState.CREATING) {
            Instant now = clock.instant();
            attempt.markFailed(now);
            Registration registration = registrationRepository.findByIdForUpdate(attempt.getRegistration().getId())
                    .orElseThrow(() -> new IllegalStateException("Registration disappeared."));
            registration.fail(now);
        }
    }

    private RegistrationReservation existingReservation(Registration registration) {
        var latest = paymentAttemptRepository.findFirstByRegistrationIdOrderByAttemptNumberDesc(registration.getId());
        if (latest.isEmpty()) {
            return new RegistrationReservation(
                    registration.getId(),
                    null,
                    registration.getState(),
                    registration.getHoldExpiresAt(),
                    registration.getFeeInr(),
                    0,
                    null,
                    false);
        }
        PaymentAttempt attempt = latest.get();
        return reservationFor(registration, attempt, false);
    }

    private RegistrationReservation reservationFor(
            Registration registration,
            PaymentAttempt attempt,
            boolean createOrder) {
        return new RegistrationReservation(
                registration.getId(),
                attempt.getId(),
                registration.getState(),
                registration.getHoldExpiresAt(),
                registration.getFeeInr(),
                attempt.getAmountPaise(),
                attempt.getRazorpayOrderId(),
                createOrder);
    }

    private void requireOpenEvent(Event event, Instant now) {
        if (!isEventOpen(event, now)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registration for this event is closed.");
        }
    }

    private boolean isEventOpen(Event event, Instant now) {
        OffsetDateTime current = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        EventStatus status = event.getPublicationState();
        boolean published = status == EventStatus.PUBLISHED
                || status == EventStatus.SCHEDULED
                && event.getScheduledPublishAt() != null
                && !event.getScheduledPublishAt().isAfter(current);
        return published
                && event.getRegistrationDeadline().isAfter(current)
                && event.getStartsAt().isAfter(current);
    }

}
