package in.soaringeagles.api.integrations.payment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final RazorpayProperties properties;

    RazorpayPaymentGateway(RestClient.Builder restClientBuilder, RazorpayProperties properties) {
        String baseUrl = properties.apiBaseUrl() == null || properties.apiBaseUrl().isBlank()
                ? "https://api.razorpay.com/v1"
                : properties.apiBaseUrl();
        restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.properties = properties;
    }

    @Override
    public boolean isCheckoutConfigured() {
        return properties.isCheckoutConfigured();
    }

    @Override
    public boolean isWebhookConfigured() {
        return properties.isWebhookConfigured();
    }

    @Override
    public String createOrder(String receipt, long amountPaise, String currency, String registrationId) {
        if (!isCheckoutConfigured()) {
            throw new PaymentProviderUnavailableException();
        }
        try {
            RazorpayOrder order = restClient.post()
                    .uri("/orders")
                    .headers(headers -> headers.setBasicAuth(properties.keyId(), properties.keySecret()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "amount", amountPaise,
                            "currency", currency,
                            "receipt", receipt,
                            "notes", Map.of("registration_id", registrationId)))
                    .retrieve()
                    .body(RazorpayOrder.class);
            if (order == null || order.id() == null || order.id().isBlank()) {
                throw new PaymentProviderUnavailableException();
            }
            return order.id();
        } catch (RestClientException exception) {
            throw new PaymentProviderUnavailableException(exception);
        }
    }

    @Override
    public boolean isWebhookSignatureValid(byte[] rawBody, String signature) {
        if (!properties.isWebhookConfigured() || signature == null || signature.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.webhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            byte[] expected = HexFormat.of().formatHex(mac.doFinal(rawBody))
                    .getBytes(StandardCharsets.US_ASCII);
            byte[] supplied = signature.getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(expected, supplied);
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to verify Razorpay webhook signature.", exception);
        }
    }

    @Override
    public String checkoutKeyId() {
        return properties.keyId();
    }

    private record RazorpayOrder(String id) {
    }
}
