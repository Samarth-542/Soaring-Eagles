package in.soaringeagles.api.events;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class PublicEventController {

    private final EventService eventService;

    PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<PublicEventView>> list() {
        return ResponseEntity.ok()
                .cacheControl(publicEventCache())
                .body(eventService.listPublic());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicEventView> get(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .cacheControl(publicEventCache())
                .body(eventService.getPublic(id));
    }

    private CacheControl publicEventCache() {
        return CacheControl.maxAge(Duration.ofSeconds(20))
                .cachePublic()
                .staleWhileRevalidate(Duration.ofSeconds(60));
    }
}
