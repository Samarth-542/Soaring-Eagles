package in.soaringeagles.api.publiccontent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/public-content")
public class AdminPublicContentController {

    private final PublicContentSnapshotPublisher publisher;

    AdminPublicContentController(PublicContentSnapshotPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publish")
    public PublicContentSnapshotPublisher.SnapshotManifest publish() {
        return publisher.publish();
    }
}
