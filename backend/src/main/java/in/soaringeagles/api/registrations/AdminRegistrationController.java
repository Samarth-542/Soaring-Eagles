package in.soaringeagles.api.registrations;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
public class AdminRegistrationController {

    private final RegistrationAdminService registrationAdminService;

    AdminRegistrationController(RegistrationAdminService registrationAdminService) {
        this.registrationAdminService = registrationAdminService;
    }

    @GetMapping("/events/{eventId}/registrations")
    public Page<AdminRegistrationView> list(
            @PathVariable UUID eventId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return registrationAdminService.list(eventId, page, size);
    }

    @GetMapping("/registrations/{registrationId}")
    public AdminRegistrationView get(@PathVariable UUID registrationId) {
        return registrationAdminService.get(registrationId);
    }

    @PutMapping("/registrations/{registrationId}")
    public AdminRegistrationView update(
            @PathVariable UUID registrationId,
            @Valid @RequestBody RegistrationRequest request) {
        return registrationAdminService.update(registrationId, request);
    }

    @PostMapping("/registrations/{registrationId}/email/retry")
    public ResponseEntity<Void> retryEmail(@PathVariable UUID registrationId) {
        registrationAdminService.retryConfirmationEmail(registrationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/registrations/export.csv")
    public ResponseEntity<ByteArrayResource> export(@PathVariable UUID eventId) {
        ByteArrayResource csv = new ByteArrayResource(registrationAdminService.exportCsv(eventId));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("registrations-" + eventId + ".csv")
                                .build()
                                .toString())
                .contentLength(csv.contentLength())
                .body(csv);
    }
}
