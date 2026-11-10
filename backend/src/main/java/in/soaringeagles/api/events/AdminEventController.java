package in.soaringeagles.api.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import in.soaringeagles.api.publiccontent.PublicContentSnapshotPublisher;
import in.soaringeagles.api.publiccontent.ScheduledEventSnapshotPublisher;
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
    private final PublicContentSnapshotPublisher snapshotPublisher;
    private final ScheduledEventSnapshotPublisher scheduledSnapshotPublisher;

    AdminEventController(
            EventService eventService,
            PublicContentSnapshotPublisher snapshotPublisher,
            ScheduledEventSnapshotPublisher scheduledSnapshotPublisher) {
        this.eventService = eventService;
        this.snapshotPublisher = snapshotPublisher;
        this.scheduledSnapshotPublisher = scheduledSnapshotPublisher;
    }

    @GetMapping
    public List<EventView> list() {
        return eventService.listAdmin();
    }

    @PostMapping
    public ResponseEntity<EventView> create(@Valid @RequestBody EventRequest request) {
        EventView event = eventService.create(request);
        scheduledSnapshotPublisher.syncAfterMutation(event);
        publishIfPublic(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/{id}")
    public EventView update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        EventView event = eventService.update(id, request);
        scheduledSnapshotPublisher.syncAfterMutation(event);
        publishIfPublic(event);
        return event;
    }

    @PostMapping("/{id}/schedule")
    public EventView schedule(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleRequest request) {
        EventView event = eventService.schedule(id, request.publishAt());
        scheduledSnapshotPublisher.syncAfterMutation(event);
        publishIfPublic(event);
        return event;
    }

    @PostMapping("/{id}/publish")
    public EventView publish(@PathVariable UUID id) {
        EventView event = eventService.publish(id);
        scheduledSnapshotPublisher.syncAfterMutation(event);
        publishIfPublic(event);
        return event;
    }

    @PostMapping("/{id}/close")
    public EventView close(@PathVariable UUID id) {
        EventView event = eventService.close(id);
        scheduledSnapshotPublisher.syncAfterMutation(event);
        publishIfPublic(event);
        return event;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean wasPublic = eventService.delete(id);
        scheduledSnapshotPublisher.cancelAfterMutation(id);
        if (wasPublic) snapshotPublisher.publishAfterMutation();
        return ResponseEntity.noContent().build();
    }

    private void publishIfPublic(EventView event) {
        if (event.status() == EventStatus.PUBLISHED || event.status() == EventStatus.CLOSED) {
            snapshotPublisher.publishAfterMutation();
        }
    }

    public record ScheduleRequest(@NotNull OffsetDateTime publishAt) {
    }
}
