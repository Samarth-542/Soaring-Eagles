package in.soaringeagles.api.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Column(nullable = false, length = 240)
    private String venue;

    @Column(name = "poster_url", length = 2048)
    private String posterUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String eligibility;

    @Column(name = "fee_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal feeInr;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_type", nullable = false, length = 20)
    private RegistrationType registrationType;

    @Column(name = "registration_deadline", nullable = false)
    private OffsetDateTime registrationDeadline;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "minimum_team_size")
    private Integer minimumTeamSize;

    @Column(name = "maximum_team_size")
    private Integer maximumTeamSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "publication_state", nullable = false, length = 20)
    private EventStatus publicationState;

    @Column(name = "scheduled_publish_at")
    private OffsetDateTime scheduledPublishAt;

    @Column(name = "archive_story", columnDefinition = "text")
    private String archiveStory;

    @Column(name = "archive_results", columnDefinition = "text")
    private String archiveResults;

    @ElementCollection
    @CollectionTable(name = "event_archive_photos", joinColumns = @JoinColumn(name = "event_id", nullable = false))
    @OrderColumn(name = "position")
    @Column(name = "url", nullable = false, length = 2048)
    private List<String> archivePhotos = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Event() {
    }

    Event(EventRequest request) {
        updateDetails(request);
        publicationState = EventStatus.DRAFT;
    }

    void updateDetails(EventRequest request) {
        title = request.title().trim();
        description = request.description().trim();
        startsAt = request.startsAt();
        endsAt = request.endsAt();
        venue = request.venue().trim();
        posterUrl = normalizeOptional(request.posterUrl());
        eligibility = request.eligibility().trim();
        feeInr = request.feeInr();
        registrationType = request.registrationType();
        registrationDeadline = request.registrationDeadline();
        capacity = request.capacity();
        minimumTeamSize = request.minimumTeamSize();
        maximumTeamSize = request.maximumTeamSize();
        archiveStory = normalizeOptional(request.archiveStory());
        archiveResults = normalizeOptional(request.archiveResults());
        archivePhotos.clear();
        if (request.archivePhotos() != null) {
            archivePhotos.addAll(request.archivePhotos().stream()
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .toList());
        }
    }

    void schedule(OffsetDateTime publishAt) {
        publicationState = EventStatus.SCHEDULED;
        scheduledPublishAt = publishAt;
    }

    void publish() {
        publicationState = EventStatus.PUBLISHED;
        scheduledPublishAt = null;
    }

    void close() {
        publicationState = EventStatus.CLOSED;
        scheduledPublishAt = null;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public String getVenue() { return venue; }
    public String getPosterUrl() { return posterUrl; }
    public String getEligibility() { return eligibility; }
    public BigDecimal getFeeInr() { return feeInr; }
    public RegistrationType getRegistrationType() { return registrationType; }
    public OffsetDateTime getRegistrationDeadline() { return registrationDeadline; }
    public int getCapacity() { return capacity; }
    public Integer getMinimumTeamSize() { return minimumTeamSize; }
    public Integer getMaximumTeamSize() { return maximumTeamSize; }
    public EventStatus getPublicationState() { return publicationState; }
    public OffsetDateTime getScheduledPublishAt() { return scheduledPublishAt; }
    public String getArchiveStory() { return archiveStory; }
    public String getArchiveResults() { return archiveResults; }
    public List<String> getArchivePhotos() { return List.copyOf(archivePhotos); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
