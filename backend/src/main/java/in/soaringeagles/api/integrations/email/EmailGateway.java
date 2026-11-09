package in.soaringeagles.api.integrations.email;

public interface EmailGateway {

    String send(String idempotencyKey, EmailMessage message);
}
