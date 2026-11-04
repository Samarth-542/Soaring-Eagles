package in.soaringeagles.api.registrations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {

    Optional<Registration> findByEventIdAndIdempotencyKey(UUID eventId, UUID idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select registration from Registration registration where registration.id = :id")
    Optional<Registration> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select count(registration) from Registration registration
            where registration.event.id = :eventId
              and (
                  registration.state = in.soaringeagles.api.registrations.RegistrationState.CONFIRMED
                  or (
                      registration.state = in.soaringeagles.api.registrations.RegistrationState.PENDING_PAYMENT
                      and registration.holdExpiresAt > :now
                  )
              )
            """)
    long countCapacitySlotsInUse(@Param("eventId") UUID eventId, @Param("now") Instant now);

    @Modifying
    @Query("""
            update Registration registration
            set registration.state = in.soaringeagles.api.registrations.RegistrationState.EXPIRED,
                registration.holdExpiresAt = null,
                registration.updatedAt = :now
            where registration.state = in.soaringeagles.api.registrations.RegistrationState.PENDING_PAYMENT
              and registration.holdExpiresAt <= :now
            """)
    int expireHolds(@Param("now") Instant now);

    Page<Registration> findAllByEventIdOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    List<Registration> findAllByEventIdOrderByCreatedAtDesc(UUID eventId);
}
