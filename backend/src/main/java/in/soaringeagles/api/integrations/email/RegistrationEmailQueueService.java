package in.soaringeagles.api.integrations.email;

import java.time.Clock;
import java.util.UUID;

import in.soaringeagles.api.registrations.RegistrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationEmailQueueService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationEmailDeliveryRepository deliveryRepository;
    private final Clock clock;

    RegistrationEmailQueueService(
            RegistrationRepository registrationRepository,
            RegistrationEmailDeliveryRepository deliveryRepository,
            Clock clock) {
        this.registrationRepository = registrationRepository;
        this.deliveryRepository = deliveryRepository;
        this.clock = clock;
    }

    @Transactional
    public void queue(UUID registrationId) {
        if (deliveryRepository.existsByRegistrationId(registrationId)) return;
        var registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalStateException("Registration for confirmation email was not found."));
        deliveryRepository.saveAndFlush(new RegistrationEmailDelivery(registration, clock.instant()));
    }

    @Transactional
    public void retry(UUID registrationId) {
        RegistrationEmailDelivery delivery = deliveryRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email delivery was not found."));
        if (delivery.getState() == EmailDeliveryState.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The confirmation email has already been delivered.");
        }
        delivery.retryNow(clock.instant());
    }

    @Transactional(readOnly = true)
    public RegistrationEmailDelivery findByRegistrationId(UUID registrationId) {
        return deliveryRepository.findByRegistrationId(registrationId).orElse(null);
    }
}
