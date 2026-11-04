package in.soaringeagles.api.integrations.email;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistrationEmailDeliveryRepository
        extends JpaRepository<RegistrationEmailDelivery, UUID> {

    boolean existsByRegistrationId(UUID registrationId);

    Optional<RegistrationEmailDelivery> findByRegistrationId(UUID registrationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select delivery from RegistrationEmailDelivery delivery
            where (
                delivery.state in (
                    in.soaringeagles.api.integrations.email.EmailDeliveryState.PENDING,
                    in.soaringeagles.api.integrations.email.EmailDeliveryState.FAILED
                )
                and delivery.nextAttemptAt <= :now
            ) or (
                delivery.state = in.soaringeagles.api.integrations.email.EmailDeliveryState.SENDING
                and delivery.updatedAt <= :staleBefore
            )
            order by delivery.createdAt asc
            """)
    List<RegistrationEmailDelivery> findDueForUpdate(
            @Param("now") Instant now,
            @Param("staleBefore") Instant staleBefore,
            org.springframework.data.domain.Pageable pageable);
}
