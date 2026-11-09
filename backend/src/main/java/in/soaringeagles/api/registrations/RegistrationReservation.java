package in.soaringeagles.api.registrations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record RegistrationReservation(
        UUID registrationId,
        UUID paymentAttemptId,
        RegistrationState state,
        Instant holdExpiresAt,
        BigDecimal feeInr,
        long amountPaise,
        String razorpayOrderId,
        boolean needsOrderCreation,
        in.soaringeagles.api.events.PaymentMethod paymentMethod) {
}
