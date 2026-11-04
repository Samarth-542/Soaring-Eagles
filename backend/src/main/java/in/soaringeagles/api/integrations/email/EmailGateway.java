package in.soaringeagles.api.integrations.email;

import java.util.UUID;

public interface EmailGateway {

    String send(UUID deliveryId, EmailMessage message);
}
