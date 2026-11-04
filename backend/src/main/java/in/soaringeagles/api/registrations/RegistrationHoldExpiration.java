package in.soaringeagles.api.registrations;

import java.time.Clock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistrationHoldExpiration {

    private final RegistrationRepository registrationRepository;
    private final Clock clock;

    RegistrationHoldExpiration(RegistrationRepository registrationRepository, Clock clock) {
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.registration.expiration-scan-ms:30000}")
    @Transactional
    public void expireHolds() {
        registrationRepository.expireHolds(clock.instant());
    }
}
