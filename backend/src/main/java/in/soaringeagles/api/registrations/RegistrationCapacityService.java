package in.soaringeagles.api.registrations;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
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

    public Map<UUID, Integer> remainingCapacities(Map<UUID, Integer> capacities, OffsetDateTime now) {
        if (capacities.isEmpty()) return Map.of();

        Map<UUID, Long> usedSlots = new HashMap<>();
        for (RegistrationRepository.CapacityUsage usage : registrationRepository
                .countCapacitySlotsInUseByEventIds(capacities.keySet(), now.toInstant())) {
            usedSlots.put(usage.getEventId(), usage.getUsedSlots());
        }

        Map<UUID, Integer> remaining = new HashMap<>();
        capacities.forEach((eventId, capacity) -> remaining.put(
                eventId, (int) Math.max(0, capacity - usedSlots.getOrDefault(eventId, 0L))));
        return remaining;
    }

    long countCapacitySlotsInUse(UUID eventId, Instant now) {
        return registrationRepository.countCapacitySlotsInUse(eventId, now);
    }
}
