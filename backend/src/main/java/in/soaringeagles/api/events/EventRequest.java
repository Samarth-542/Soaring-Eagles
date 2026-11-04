package in.soaringeagles.api.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 12000) String description,
        @NotNull OffsetDateTime startsAt,
        @NotNull OffsetDateTime endsAt,
        @NotBlank @Size(max = 240) String venue,
        @Size(max = 2048) String posterUrl,
        @NotBlank @Size(max = 3000) String eligibility,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal feeInr,
        @NotNull RegistrationType registrationType,
        @NotNull OffsetDateTime registrationDeadline,
        @NotNull @Positive Integer capacity,
        Integer minimumTeamSize,
        Integer maximumTeamSize,
        @Size(max = 12000) String archiveStory,
        @Size(max = 12000) String archiveResults,
        @Size(max = 20) List<@NotBlank @Size(max = 2048) String> archivePhotos) {
}
