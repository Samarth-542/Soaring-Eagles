package in.soaringeagles.api.publiccontent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.soaringeagles.api.content.ContentService;
import in.soaringeagles.api.content.SiteDetailsService;
import in.soaringeagles.api.content.SupabaseStorageProperties;
import in.soaringeagles.api.events.EventService;
import in.soaringeagles.api.events.EventStatus;
import in.soaringeagles.api.events.PaymentMethod;
import in.soaringeagles.api.events.PublicEventView;
import in.soaringeagles.api.events.RegistrationType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class PublicContentSnapshotPublisherTest {

    private static final List<String> SNAPSHOTS = List.of(
            "events.json", "team.json", "projects.json", "journey.json", "site-details.json");

    @Test
    void verifiesAllVersionedFilesAndManifestBeforeUpdatingRootManifest() throws Exception {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Map<String, byte[]> uploadedFiles = new HashMap<>();
        SNAPSHOTS.forEach(name -> {
            expectUpload(server, uploadedFiles, "/snapshots/", withSuccess());
            expectVerification(server, uploadedFiles, "/snapshots/");
        });
        expectUpload(server, uploadedFiles, "/snapshots/", withSuccess());
        expectVerification(server, uploadedFiles, "/snapshots/");
        expectUpload(server, uploadedFiles, "/manifest.json", withSuccess());

        PublicContentSnapshotPublisher publisher = publisher(builder);
        PublicContentSnapshotPublisher.SnapshotManifest manifest = publisher.publish();

        assertThat(manifest.snapshots()).hasSize(5);
        assertThat(uploadedFiles).containsKey("snapshots/" + manifest.version() + "/manifest.json");
        assertThat(uploadedFiles).containsKey("manifest.json");
        assertThat(new ObjectMapper().readTree(
                uploadedFiles.get("snapshots/" + manifest.version() + "/team.json"))
                .path("data").has("groups")).isTrue();
        server.verify();
    }

    @Test
    void failedVersionedUploadDoesNotReplaceTheExistingManifest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        Map<String, byte[]> uploadedFiles = new HashMap<>();
        expectUpload(server, uploadedFiles, "/snapshots/", withSuccess());
        expectVerification(server, uploadedFiles, "/snapshots/");
        expectUpload(server, uploadedFiles, "/snapshots/", withSuccess());
        expectVerification(server, uploadedFiles, "/snapshots/");
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/snapshots/")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        PublicContentSnapshotPublisher publisher = publisher(builder);

        assertThatThrownBy(publisher::publish)
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("The previous manifest was retained");
        assertThat(uploadedFiles).doesNotContainKey("manifest.json");
        server.verify();
    }

    @Test
    void eventSnapshotContainsOnlyPublicPresentationFields() throws Exception {
        OffsetDateTime startsAt = OffsetDateTime.parse("2026-11-20T10:00:00+05:30");
        PublicEventView event = new PublicEventView(
                UUID.randomUUID(),
                "Public title",
                "Public description",
                startsAt,
                startsAt.plusHours(1),
                "Public venue",
                "https://storage.example/poster.webp",
                "Private eligibility",
                BigDecimal.valueOf(500),
                RegistrationType.TEAM,
                PaymentMethod.RAZORPAY,
                null,
                null,
                startsAt.minusDays(1),
                100,
                42,
                2,
                10,
                EventStatus.PUBLISHED,
                false,
                true,
                true,
                "Archive story",
                "Archive results",
                List.of());

        String snapshot = new ObjectMapper().findAndRegisterModules().writeValueAsString(
                PublicContentSnapshotPublisher.PublicEventSnapshot.from(event));

        assertThat(snapshot)
                .contains("Public title", "Public description", "Public venue", "PUBLISHED")
                .doesNotContain(
                        "eligibility",
                        "feeInr",
                        "registrationType",
                        "paymentMethod",
                        "capacity",
                        "remainingCapacity",
                        "registrationDeadline",
                        "registrationWindowOpen",
                        "onlineRegistrationAvailable");
    }

    private PublicContentSnapshotPublisher publisher(RestClient.Builder builder) {
        ContentService contentService = mock(ContentService.class);
        when(contentService.listGroups()).thenReturn(List.of());
        when(contentService.listTeam(true)).thenReturn(List.of());
        when(contentService.listProjects(true)).thenReturn(List.of());
        when(contentService.listJourney(true)).thenReturn(List.of());

        EventService eventService = mock(EventService.class);
        when(eventService.listPublic()).thenReturn(List.of());

        SiteDetailsService siteDetailsService = mock(SiteDetailsService.class);
        when(siteDetailsService.get()).thenReturn(new SiteDetailsService.SiteDetails(
                "contact@example.com", "", "", "Chennai"));

        return new PublicContentSnapshotPublisher(
                builder,
                new ObjectMapper().findAndRegisterModules(),
                new SupabaseStorageProperties(
                        "https://storage.example", "service-role-key", "images", "private"),
                contentService,
                eventService,
                siteDetailsService,
                Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC),
                "public-content",
                true);
    }

    private void expectUpload(
            MockRestServiceServer server,
            Map<String, byte[]> uploadedFiles,
            String pathFragment,
            ResponseCreator response) {
        server.expect(requestTo(org.hamcrest.Matchers.containsString(pathFragment)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    uploadedFiles.put(
                            relativePath(request.getURI().getPath()),
                            ((MockClientHttpRequest) request).getBodyAsBytes());
                    return response.createResponse(request);
                });
    }

    private void expectVerification(
            MockRestServiceServer server,
            Map<String, byte[]> uploadedFiles,
            String pathFragment) {
        server.expect(requestTo(org.hamcrest.Matchers.containsString(pathFragment)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> new MockClientHttpResponse(
                        uploadedFiles.get(relativePath(request.getURI().getPath())),
                        HttpStatus.OK));
    }

    private String relativePath(String path) {
        return path.substring(path.indexOf("/public-content/") + "/public-content/".length());
    }
}
