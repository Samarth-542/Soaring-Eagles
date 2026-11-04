package in.soaringeagles.api.integrations.email;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailDeliveryClaimService {

    private final RegistrationEmailDeliveryRepository deliveryRepository;
    private final Clock clock;

    EmailDeliveryClaimService(RegistrationEmailDeliveryRepository deliveryRepository, Clock clock) {
        this.deliveryRepository = deliveryRepository;
        this.clock = clock;
    }

    @Transactional
    public List<UUID> claimDue(int batchSize) {
        var now = clock.instant();
        return deliveryRepository.findDueForUpdate(now, now.minus(Duration.ofMinutes(5)), PageRequest.of(0, batchSize))
                .stream()
                .map(delivery -> {
                    delivery.markSending(now);
                    return delivery.getId();
                })
                .toList();
    }

    @Transactional
    public void markDelivered(UUID deliveryId, String resendEmailId) {
        deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalStateException("Email delivery disappeared."))
                .markDelivered(resendEmailId, clock.instant());
    }

    @Transactional
    public void markFailed(UUID deliveryId) {
        var now = clock.instant();
        deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalStateException("Email delivery disappeared."))
                .markFailed(now.plus(Duration.ofMinutes(5)), now);
    }
}
