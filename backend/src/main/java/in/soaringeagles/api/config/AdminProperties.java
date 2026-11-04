package in.soaringeagles.api.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        @NotBlank @Email String email,
        @NotBlank @Pattern(
                regexp = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$",
                message = "must be a BCrypt password hash")
        String passwordHash) {

    public String normalizedEmail() {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
