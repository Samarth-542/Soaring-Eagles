package in.soaringeagles.api.registrations;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/v1/events/{eventId}/registrations")
    public ResponseEntity<CheckoutStartResponse> start(
            @PathVariable UUID eventId,
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationService.start(eventId, idempotencyKey, request));
    }

    @PostMapping("/api/v1/registrations/{registrationId}/retry")
    public CheckoutStartResponse retry(@PathVariable UUID registrationId) {
        return registrationService.retry(registrationId);
    }

    @GetMapping("/api/v1/registrations/{registrationId}/status")
    public RegistrationStatusView status(@PathVariable UUID registrationId) {
        return registrationService.status(registrationId);
    }
}
