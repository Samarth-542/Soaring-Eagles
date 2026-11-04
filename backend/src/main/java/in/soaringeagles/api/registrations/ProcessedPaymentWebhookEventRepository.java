package in.soaringeagles.api.registrations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentWebhookEventRepository
        extends JpaRepository<ProcessedPaymentWebhookEvent, String> {
}
