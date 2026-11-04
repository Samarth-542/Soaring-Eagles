package in.soaringeagles.api.registrations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CheckoutStartResponse(
        UUID registrationId,
        RegistrationState status,
        Instant holdExpiresAt,
        BigDecimal feeInr,
        UUID paymentAttemptId,
        String razorpayOrderId,
        String razorpayKeyId,
        Long amountPaise,
        String currency,
        boolean retryAllowed,
        String message) {
}
