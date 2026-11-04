package in.soaringeagles.api.registrations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    List<PaymentAttempt> findAllByRegistrationIdOrderByAttemptNumberDesc(UUID registrationId);

    Optional<PaymentAttempt> findFirstByRegistrationIdOrderByAttemptNumberDesc(UUID registrationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from PaymentAttempt attempt where attempt.razorpayOrderId = :orderId")
    Optional<PaymentAttempt> findByOrderIdForUpdate(@Param("orderId") String orderId);
}
