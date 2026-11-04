package in.soaringeagles.api.integrations.payment;

public class PaymentProviderUnavailableException extends RuntimeException {

    public PaymentProviderUnavailableException() {
        super("Online checkout is not configured or is temporarily unavailable.");
    }

    public PaymentProviderUnavailableException(Throwable cause) {
        super("Online checkout is not configured or is temporarily unavailable.", cause);
    }
}
