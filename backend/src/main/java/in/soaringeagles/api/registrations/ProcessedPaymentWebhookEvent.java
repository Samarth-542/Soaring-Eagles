package in.soaringeagles.api.registrations;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_payment_webhook_events")
public class ProcessedPaymentWebhookEvent {

    @Id
    @Column(name = "provider_event_id", length = 120)
    private String providerEventId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processing_state", nullable = false, length = 16)
    private String processingState;

    protected ProcessedPaymentWebhookEvent() {
    }

    public ProcessedPaymentWebhookEvent(String providerEventId, Instant receivedAt, String processingState) {
        this.providerEventId = providerEventId;
        this.receivedAt = receivedAt;
        this.processingState = processingState;
    }
}
