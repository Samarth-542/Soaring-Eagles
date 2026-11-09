package in.soaringeagles.api.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/events")
public class AdminEventController {

    private final EventService eventService;

    AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventView> list() {
        return eventService.listAdmin();
    }

    @PostMapping
    public ResponseEntity<EventView> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @PutMapping("/{id}")
    public EventView update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    @PostMapping("/{id}/schedule")
    public EventView schedule(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleRequest request) {
        return eventService.schedule(id, request.publishAt());
    }

    @PostMapping("/{id}/publish")
    public EventView publish(@PathVariable UUID id) {
        return eventService.publish(id);
    }

    @PostMapping("/{id}/close")
    public EventView close(@PathVariable UUID id) {
        return eventService.close(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public record ScheduleRequest(@NotNull OffsetDateTime publishAt) {
    }
}
