package in.soaringeagles.api.integrations.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/razorpay")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader("x-razorpay-event-id") String eventId,
            @RequestHeader("x-razorpay-signature") String signature,
            @RequestBody byte[] rawBody) {
        paymentWebhookService.process(eventId, signature, rawBody);
        return ResponseEntity.noContent().build();
    }
}
