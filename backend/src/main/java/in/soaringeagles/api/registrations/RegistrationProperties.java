package in.soaringeagles.api.registrations;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.registration")
public record RegistrationProperties(Duration holdDuration) {

    public RegistrationProperties {
        if (holdDuration == null || holdDuration.isZero() || holdDuration.isNegative()) {
            throw new IllegalArgumentException("Registration hold duration must be positive.");
        }
    }
}
