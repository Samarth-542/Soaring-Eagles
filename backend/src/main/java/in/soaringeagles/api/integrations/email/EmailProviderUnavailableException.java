package in.soaringeagles.api.integrations.email;

public class EmailProviderUnavailableException extends RuntimeException {

    public EmailProviderUnavailableException() {
        super("Confirmation email could not be sent.");
    }

    public EmailProviderUnavailableException(Throwable cause) {
        super("Confirmation email could not be sent.", cause);
    }
}
