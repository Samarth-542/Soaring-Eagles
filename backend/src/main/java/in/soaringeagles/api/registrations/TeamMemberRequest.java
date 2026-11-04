package in.soaringeagles.api.registrations;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamMemberRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull StudyYear year,
        @NotBlank @Size(max = 160) String department,
        @NotBlank @Size(max = 200) String institution,
        @NotBlank @Email @Size(max = 254) String email) {
}
