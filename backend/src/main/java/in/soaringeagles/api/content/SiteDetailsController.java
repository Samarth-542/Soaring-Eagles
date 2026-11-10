package in.soaringeagles.api.content;

import java.time.Duration;

import in.soaringeagles.api.publiccontent.PublicContentSnapshotPublisher;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteDetailsController {

    private final SiteDetailsService siteDetailsService;
    private final PublicContentSnapshotPublisher snapshotPublisher;

    SiteDetailsController(
            SiteDetailsService siteDetailsService,
            PublicContentSnapshotPublisher snapshotPublisher) {
        this.siteDetailsService = siteDetailsService;
        this.snapshotPublisher = snapshotPublisher;
    }

    @GetMapping("/api/v1/content/site-details")
    public ResponseEntity<SiteDetailsService.SiteDetails> publicSiteDetails() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))
                        .cachePublic()
                        .staleWhileRevalidate(Duration.ofMinutes(5)))
                .body(siteDetailsService.get());
    }

    @GetMapping("/api/v1/admin/site-details")
    public SiteDetailsService.SiteDetails adminSiteDetails() {
        return siteDetailsService.get();
    }

    @PutMapping("/api/v1/admin/site-details")
    public SiteDetailsService.SiteDetails update(
            @Valid @RequestBody SiteDetailsService.SiteDetailsRequest request,
            java.security.Principal actor) {
        SiteDetailsService.SiteDetails details = siteDetailsService.update(request, actor.getName());
        snapshotPublisher.publishAfterMutation();
        return details;
    }
}
