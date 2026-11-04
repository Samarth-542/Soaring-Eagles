package in.soaringeagles.api.integrations.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RazorpayPaymentGatewayTest {

    @Test
    void validatesWebhookSignatureAgainstExactRawBody() throws Exception {
        RazorpayProperties properties = new RazorpayProperties(
                "rzp_test_key",
                "api-secret",
                "webhook-secret",
                "https://api.razorpay.com/v1",
                true);
        RazorpayPaymentGateway gateway = new RazorpayPaymentGateway(RestClient.builder(), properties);
        byte[] rawBody = "{\"event\":\"payment.captured\"}".getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("webhook-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal(rawBody));
        byte[] reformattedBody = "{\"event\": \"payment.captured\"}".getBytes(StandardCharsets.UTF_8);

        assertThat(gateway.isWebhookSignatureValid(rawBody, signature)).isTrue();
        assertThat(gateway.isWebhookSignatureValid(reformattedBody, signature)).isFalse();
        assertThat(gateway.isWebhookSignatureValid(rawBody, "not-a-signature")).isFalse();
    }
}
