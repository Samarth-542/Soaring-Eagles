package in.soaringeagles.api.registrations;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RegistrationCapacityService {

    private final RegistrationRepository registrationRepository;

    RegistrationCapacityService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public int remainingCapacity(UUID eventId, int capacity, OffsetDateTime now) {
        long used = registrationRepository.countCapacitySlotsInUse(eventId, now.toInstant());
        return (int) Math.max(0, capacity - used);
    }

    long countCapacitySlotsInUse(UUID eventId, Instant now) {
        return registrationRepository.countCapacitySlotsInUse(eventId, now);
    }
}
