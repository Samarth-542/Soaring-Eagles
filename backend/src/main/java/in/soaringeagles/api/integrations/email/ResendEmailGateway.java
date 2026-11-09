package in.soaringeagles.api.integrations.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String send(String idempotencyKey, EmailMessage message) {
        if (!properties.isConfigured()) {
            throw new EmailProviderUnavailableException();
        }
        try {
            ResendResponse response = restClient.post()
                    .uri("/emails")
                    .headers(headers -> {
                        headers.setBearerAuth(properties.apiKey());
                        headers.set("Idempotency-Key", idempotencyKey);
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(message))
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

    private Map<String, Object> requestBody(EmailMessage message) {
        Map<String, Object> body = new HashMap<>();
        body.put("from", properties.fromAddress());
        body.put("to", List.of(message.to()));
        body.put("subject", message.subject());
        body.put("text", message.text());
        return body;
    }

    private record ResendResponse(String id) {
    }
}
