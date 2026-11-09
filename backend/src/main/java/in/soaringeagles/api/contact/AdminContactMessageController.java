package in.soaringeagles.api.contact;

import java.security.Principal;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/contact-messages")
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;

    AdminContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @GetMapping
    public ContactMessageService.ContactMessagePage list(
            @RequestParam(required = false) ContactMessageState state,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return contactMessageService.list(state, page, size);
    }

    @GetMapping("/{id}")
    public ContactMessageService.ContactMessageView get(@PathVariable UUID id) {
        return contactMessageService.get(id);
    }

    @PatchMapping("/{id}/state")
    public ContactMessageService.ContactMessageView updateState(
            @PathVariable UUID id,
            @Valid @RequestBody ContactMessageStateRequest request,
            Principal actor) {
        return contactMessageService.updateState(id, request.state(), actor);
    }

    public record ContactMessageStateRequest(@NotNull ContactMessageState state) {
    }
}
