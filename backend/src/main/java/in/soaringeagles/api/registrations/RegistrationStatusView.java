package in.soaringeagles.api.registrations;

import java.time.Instant;

public record RegistrationStatusView(
        RegistrationState status,
        Instant holdExpiresAt,
        boolean retryAllowed) {
}
