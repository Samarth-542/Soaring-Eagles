package in.soaringeagles.api.registrations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import in.soaringeagles.api.events.RegistrationType;
import in.soaringeagles.api.integrations.email.EmailDeliveryState;

public record AdminRegistrationView(
        UUID id,
        UUID eventId,
        String eventTitle,
        RegistrationType registrationType,
        RegistrationState status,
        BigDecimal feeInr,
        String individualName,
        String individualPhone,
        String individualEmail,
        Integer individualYear,
        String individualDepartment,
        String individualInstitution,
        String teamName,
        String captainName,
        String captainPhone,
        String captainEmail,
        String paymentProofUrl,
        List<AdminTeamMemberView> members,
        PaymentAttemptState paymentStatus,
        EmailDeliveryState emailDeliveryStatus,
        int emailDeliveryAttempts,
        String emailDeliveryError,
        String razorpayOrderId,
        String razorpayPaymentId,
        Instant holdExpiresAt,
        Instant createdAt,
        Instant updatedAt) {
}

record AdminTeamMemberView(
        String name,
        int year,
        String department,
        String institution,
        String email) {
}
