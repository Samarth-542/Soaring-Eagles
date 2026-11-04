package in.soaringeagles.api.integrations.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.payment.razorpay")
public record RazorpayProperties(
        String keyId,
        String keySecret,
        String webhookSecret,
        String apiBaseUrl,
        boolean checkoutEnabled) {

    public boolean isCheckoutConfigured() {
        return checkoutEnabled && hasText(keyId) && hasText(keySecret) && hasText(webhookSecret);
    }

    public boolean isWebhookConfigured() {
        return hasText(webhookSecret);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
