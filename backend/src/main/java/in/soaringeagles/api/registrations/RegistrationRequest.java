package in.soaringeagles.api.registrations;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @Size(max = 120) String name,
        @Size(max = 30) String phone,
        @Email @Size(max = 254) String email,
        StudyYear year,
        @Size(max = 160) String department,
        @Size(max = 200) String institution,
        @Size(max = 120) String teamName,
        @Size(max = 120) String captainName,
        @Size(max = 30) String captainPhone,
        @Email @Size(max = 254) String captainEmail,
        @Size(max = 2048) String paymentProofUrl,
        @Size(max = 30) List<@Valid TeamMemberRequest> members) {
}
