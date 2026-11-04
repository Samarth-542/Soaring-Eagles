package in.soaringeagles.api.integrations.email;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ResendEmailGateway implements EmailGateway {

    private final ResendProperties properties;
    private final RestClient restClient;

    ResendEmailGateway(RestClient.Builder builder, ResendProperties properties) {
        String baseUrl = properties.apiBaseUrl() == null || properties.apiBaseUrl().isBlank()
                ? "https://api.resend.com"
                : properties.apiBaseUrl();
        restClient = builder.baseUrl(baseUrl).build();
        this.properties = properties;
    }

    @Override
    public String send(UUID deliveryId, EmailMessage message) {
        if (!properties.isConfigured()) {
            throw new EmailProviderUnavailableException();
        }
        try {
            ResendResponse response = restClient.post()
                    .uri("/emails")
                    .headers(headers -> {
                        headers.setBearerAuth(properties.apiKey());
                        headers.set("Idempotency-Key", "registration-confirmation/" + deliveryId);
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", properties.fromAddress(),
                            "to", message.to(),
                            "subject", message.subject(),
                            "text", message.text()))
                    .retrieve()
                    .body(ResendResponse.class);
            if (response == null || response.id() == null || response.id().isBlank()) {
                throw new EmailProviderUnavailableException();
            }
            return response.id();
        } catch (RestClientException exception) {
            throw new EmailProviderUnavailableException(exception);
        }
    }

    private record ResendResponse(String id) {
    }
}
