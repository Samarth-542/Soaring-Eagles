package in.soaringeagles.api.registrations;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentAttemptService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final Clock clock;

    PaymentAttemptService(PaymentAttemptRepository paymentAttemptRepository, Clock clock) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.clock = clock;
    }

    @Transactional
    public void markReady(UUID attemptId, String razorpayOrderId) {
        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalStateException("Payment attempt disappeared."));
        if (attempt.getState() == PaymentAttemptState.CREATING) {
            attempt.markReady(razorpayOrderId, clock.instant());
        }
    }
}
