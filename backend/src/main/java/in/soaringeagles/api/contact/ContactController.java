package in.soaringeagles.api.contact;

import java.io.IOException;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/contact")
@Validated
public class ContactController {

    private final ContactMessageService contactMessageService;

    ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContactSubmission> submit(
            @RequestParam @NotBlank @Size(max = 120) String name,
            @RequestParam @NotBlank @Email @Size(max = 254) String email,
            @RequestParam @NotBlank @Size(max = 180) String subject,
            @RequestParam @NotBlank @Size(max = 5000) String message,
            @RequestPart(required = false) MultipartFile attachment) throws IOException {
        UUID id = contactMessageService.submit(name, email, subject, message, attachment);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ContactSubmission(id));
    }

    @GetMapping("/{id}/attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID id) {
        ContactMessageService.ContactAttachment attachment = contactMessageService.downloadAttachment(id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(attachment.fileName()).build().toString())
                .body(attachment.content());
    }

    public record ContactSubmission(UUID id) {
    }
}
