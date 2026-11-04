package in.soaringeagles.api.integrations.payment;

public interface PaymentGateway {

    boolean isCheckoutConfigured();

    boolean isWebhookConfigured();

    String createOrder(String receipt, long amountPaise, String currency, String registrationId);

    boolean isWebhookSignatureValid(byte[] rawBody, String signature);

    String checkoutKeyId();
}
