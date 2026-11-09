package in.soaringeagles.api.contact;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidContactAttachmentException extends RuntimeException {

    InvalidContactAttachmentException(String message) {
        super(message);
    }
}
