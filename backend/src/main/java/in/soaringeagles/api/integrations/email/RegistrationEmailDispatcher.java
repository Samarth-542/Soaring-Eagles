package in.soaringeagles.api.integrations.email;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegistrationEmailDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationEmailDispatcher.class);

    private final EmailDeliveryClaimService claimService;
    private final EmailDeliveryContentService contentService;
    private final EmailGateway emailGateway;

    RegistrationEmailDispatcher(
            EmailDeliveryClaimService claimService,
            EmailDeliveryContentService contentService,
            EmailGateway emailGateway) {
        this.claimService = claimService;
        this.contentService = contentService;
        this.emailGateway = emailGateway;
    }

    @Scheduled(fixedDelayString = "${app.email.resend.dispatch-delay-ms:30000}")
    public void dispatchPending() {
        for (UUID deliveryId : claimService.claimDue(20)) {
            try {
                String providerId = emailGateway.send(deliveryId, contentService.buildMessage(deliveryId));
                claimService.markDelivered(deliveryId, providerId);
            } catch (EmailProviderUnavailableException exception) {
                LOGGER.warn("Registration confirmation email delivery {} failed; it will be retried.", deliveryId, exception);
                claimService.markFailed(deliveryId);
            }
        }
    }
}
