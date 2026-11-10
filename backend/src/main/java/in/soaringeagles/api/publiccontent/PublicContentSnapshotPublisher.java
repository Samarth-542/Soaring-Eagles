package in.soaringeagles.api.publiccontent;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.soaringeagles.api.content.ContentRecords.JourneyMilestone;
import in.soaringeagles.api.content.ContentRecords.Project;
import in.soaringeagles.api.content.ContentRecords.TeamGroup;
import in.soaringeagles.api.content.ContentRecords.TeamMember;
import in.soaringeagles.api.content.ContentService;
import in.soaringeagles.api.content.SiteDetailsService;
import in.soaringeagles.api.content.SupabaseStorageProperties;
import in.soaringeagles.api.events.EventStatus;
import in.soaringeagles.api.events.EventService;
import in.soaringeagles.api.events.PublicEventView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicContentSnapshotPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PublicContentSnapshotPublisher.class);
    private static final String VERSIONED_CACHE_CONTROL = "public, max-age=31536000, immutable";
    private static final String MANIFEST_CACHE_CONTROL = "public, max-age=20, stale-while-revalidate=30";
    private static final List<String> SNAPSHOT_NAMES = List.of(
            "events.json", "team.json", "projects.json", "journey.json", "site-details.json");
    private final AtomicBoolean missingConfigurationWarningLogged = new AtomicBoolean();

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SupabaseStorageProperties storageProperties;
    private final ContentService contentService;
    private final EventService eventService;
    private final SiteDetailsService siteDetailsService;
    private final Clock clock;
    private final String bucket;
    private final boolean publishingEnabled;

    PublicContentSnapshotPublisher(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            SupabaseStorageProperties storageProperties,
            ContentService contentService,
            EventService eventService,
            SiteDetailsService siteDetailsService,
            Clock clock,
            @Value("${app.supabase.storage.public-content-bucket:public-content}") String bucket,
            @Value("${app.supabase.public-content-publishing-enabled:true}") boolean publishingEnabled) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
        this.storageProperties = storageProperties;
        this.contentService = contentService;
        this.eventService = eventService;
        this.siteDetailsService = siteDetailsService;
        this.clock = clock;
        this.bucket = bucket;
        this.publishingEnabled = publishingEnabled;
    }

    public synchronized SnapshotManifest publish() {
        if (!publishingEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Public snapshot publishing is disabled by configuration.");
        }
        if (!storageProperties.isConfigured() || bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Public snapshots require SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, and "
                            + "SUPABASE_STORAGE_PUBLIC_CONTENT_BUCKET on the backend.");
        }

        String version = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'")
                .withZone(java.time.ZoneOffset.UTC)
                .format(clock.instant()) + "-" + UUID.randomUUID();
        Instant generatedAt = clock.instant();
        Map<String, byte[]> documents = generateSnapshotDocuments(version, generatedAt);
        Map<String, SnapshotFile> snapshotFiles = new LinkedHashMap<>();

        try {
            for (String name : SNAPSHOT_NAMES) {
                byte[] content = documents.get(name);
                String key = "snapshots/" + version + "/" + name;
                upload(key, content, VERSIONED_CACHE_CONTROL, false);
                verifyUpload(key, content);
                snapshotFiles.put(name, new SnapshotFile(key, sha256(content)));
            }

            SnapshotManifest manifest = new SnapshotManifest(version, generatedAt, snapshotFiles);
            byte[] manifestContent = serialize(manifest);
            String versionedManifest = "snapshots/" + version + "/manifest.json";
            upload(versionedManifest, manifestContent, VERSIONED_CACHE_CONTROL, false);
            verifyUpload(versionedManifest, manifestContent);
            upload("manifest.json", manifestContent, MANIFEST_CACHE_CONTROL, true);
            return manifest;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Public snapshots could not be uploaded. The previous manifest was retained.",
                    exception);
        }
    }

    public void publishAfterMutation() {
        runAfterCommit(this::publishBestEffort);
    }

    private void publishBestEffort() {
        if (!publishingEnabled) {
            if (missingConfigurationWarningLogged.compareAndSet(false, true)) {
                logger.warn("Public snapshot publishing is disabled by configuration.");
            }
            return;
        }
        if (!storageProperties.isConfigured() || bucket == null || bucket.isBlank()) {
            if (missingConfigurationWarningLogged.compareAndSet(false, true)) {
                logger.warn("Public snapshot publishing is disabled until Supabase Storage is configured.");
            }
            return;
        }

        try {
            publish();
        } catch (ResponseStatusException | DataAccessException exception) {
            logger.error("Public content changed, but snapshot publishing failed; use the admin retry action.",
                    exception);
        }
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private Map<String, byte[]> generateSnapshotDocuments(String version, Instant generatedAt) {
        Map<String, byte[]> documents = new LinkedHashMap<>();
        try {
            documents.put("events.json", serialize(new SnapshotDocument<>(
                    version, generatedAt, eventService.listPublic().stream()
                            .map(PublicEventSnapshot::from)
                            .toList())));
            documents.put("team.json", serialize(new SnapshotDocument<>(
                    version, generatedAt, teamSnapshot())));
            documents.put("projects.json", serialize(new SnapshotDocument<>(
                    version, generatedAt, contentService.listProjects(true).stream()
                            .map(ProjectSnapshot::from)
                            .toList())));
            documents.put("journey.json", serialize(new SnapshotDocument<>(
                    version, generatedAt, contentService.listJourney(true).stream()
                            .map(JourneySnapshot::from)
                            .toList())));
            documents.put("site-details.json", serialize(new SnapshotDocument<>(
                    version, generatedAt, siteDetailsService.get())));
            return documents;
        } catch (DataAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Public snapshots could not be generated from the current database state.",
                    exception);
        }
    }

    private TeamDirectorySnapshot teamSnapshot() {
        List<TeamMember> members = contentService.listTeam(true);
        List<TeamGroupSnapshot> groups = contentService.listGroups().stream()
                .map(group -> new TeamGroupSnapshot(
                        group.id(),
                        group.title(),
                        group.label(),
                        members.stream()
                                .filter(member -> member.groupId().equals(group.id()))
                                .map(TeamMemberSnapshot::from)
                                .toList()))
                .toList();
        return new TeamDirectorySnapshot(groups);
    }

    private void upload(String key, byte[] content, String cacheControl, boolean upsert) {
        restClient.post()
                .uri(storageObjectUri(key))
                .header("apikey", storageProperties.serviceRoleKey())
                .header("Authorization", "Bearer " + storageProperties.serviceRoleKey())
                .header("x-upsert", Boolean.toString(upsert))
                .header("Cache-Control", cacheControl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(content)
                .retrieve()
                .toBodilessEntity();
    }

    private void verifyUpload(String key, byte[] expected) {
        byte[] actual = restClient.get()
                .uri(storageObjectUri(key))
                .header("apikey", storageProperties.serviceRoleKey())
                .header("Authorization", "Bearer " + storageProperties.serviceRoleKey())
                .retrieve()
                .body(byte[].class);
        if (actual == null || !MessageDigest.isEqual(expected, actual)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "A versioned public snapshot could not be verified; the previous manifest was retained.");
        }
    }

    private URI storageObjectUri(String key) {
        String encodedBucket = java.net.URLEncoder.encode(bucket, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create(storageProperties.normalizedUrl() + "/storage/v1/object/"
                + encodedBucket + "/" + key);
    }

    private byte[] serialize(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Public snapshot JSON could not be generated.",
                    exception);
        }
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    public record SnapshotDocument<T>(String version, Instant generatedAt, T data) {
    }

    public record SnapshotManifest(
            String version,
            Instant generatedAt,
            Map<String, SnapshotFile> snapshots) {
    }

    public record SnapshotFile(String path, String sha256) {
    }

    public record TeamGroupSnapshot(
            String id,
            String title,
            String label,
            List<TeamMemberSnapshot> members) {
    }

    public record TeamDirectorySnapshot(List<TeamGroupSnapshot> groups) {
    }

    public record TeamMemberSnapshot(
            UUID id,
            String groupId,
            String groupTitle,
            String groupLabel,
            String name,
            String role,
            String imageUrl,
            String linkedinUrl,
            String instagramUrl,
            String xUrl,
            int position) {
        static TeamMemberSnapshot from(TeamMember member) {
            return new TeamMemberSnapshot(
                    member.id(), member.groupId(), member.groupTitle(), member.groupLabel(),
                    member.name(), member.role(), member.imageUrl(), member.linkedinUrl(),
                    member.instagramUrl(), member.xUrl(), member.position());
        }
    }

    public record ProjectSnapshot(
            UUID id,
            String title,
            int year,
            String description,
            String imageUrl,
            List<String> tags,
            int position) {
        static ProjectSnapshot from(Project project) {
            return new ProjectSnapshot(
                    project.id(), project.title(), project.year(), project.description(),
                    project.imageUrl(), project.tags(), project.position());
        }
    }

    public record JourneySnapshot(
            UUID id,
            int year,
            String title,
            String description,
            String imageUrl,
            int position) {
        static JourneySnapshot from(JourneyMilestone milestone) {
            return new JourneySnapshot(
                    milestone.id(), milestone.year(), milestone.title(), milestone.description(),
                    milestone.imageUrl(), milestone.position());
        }
    }

    public record PublicEventSnapshot(
            UUID id,
            String title,
            String description,
            java.time.OffsetDateTime startsAt,
            java.time.OffsetDateTime endsAt,
            String venue,
            String posterUrl,
            EventStatus status,
            String archiveStory,
            String archiveResults,
            List<String> archivePhotos) {
        static PublicEventSnapshot from(PublicEventView event) {
            return new PublicEventSnapshot(
                    event.id(), event.title(), event.description(), event.startsAt(), event.endsAt(),
                    event.venue(), event.posterUrl(), event.status(), event.archiveStory(),
                    event.archiveResults(), new ArrayList<>(event.archivePhotos()));
        }
    }
}
