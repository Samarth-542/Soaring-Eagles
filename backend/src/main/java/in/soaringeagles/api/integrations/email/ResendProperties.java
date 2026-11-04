package in.soaringeagles.api.integrations.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.email.resend")
public record ResendProperties(String apiKey, String fromAddress, String apiBaseUrl) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && fromAddress != null && !fromAddress.isBlank();
    }
}
