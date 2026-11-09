package in.soaringeagles.api.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PublicEventView(
        UUID id,
        String title,
        String description,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        String venue,
        String posterUrl,
        String eligibility,
        BigDecimal feeInr,
        RegistrationType registrationType,
        PaymentMethod paymentMethod,
        String googleFormLink,
        String googleFormQrUrl,
        OffsetDateTime registrationDeadline,
        int capacity,
        Integer remainingCapacity,
        Integer minimumTeamSize,
        Integer maximumTeamSize,
        EventStatus status,
        boolean ended,
        boolean registrationWindowOpen,
        boolean onlineRegistrationAvailable,
        String archiveStory,
        String archiveResults,
        List<String> archivePhotos) {
}
