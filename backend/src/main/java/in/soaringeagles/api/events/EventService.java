package in.soaringeagles.api.events;

import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import in.soaringeagles.api.registrations.RegistrationCapacityService;
import in.soaringeagles.api.integrations.payment.PaymentGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationCapacityService registrationCapacityService;
    private final PaymentGateway paymentGateway;
    private final Clock clock;

    EventService(
            EventRepository eventRepository,
            RegistrationCapacityService registrationCapacityService,
            PaymentGateway paymentGateway,
            Clock clock) {
        this.eventRepository = eventRepository;
        this.registrationCapacityService = registrationCapacityService;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    public EventView create(EventRequest request) {
        validateRequest(request);
        return toView(eventRepository.save(new Event(request)), nowUtc());
    }

    public EventView update(UUID id, EventRequest request) {
        validateRequest(request);
        Event event = findById(id);
        if (event.getPublicationState() == EventStatus.SCHEDULED
                && event.getScheduledPublishAt() != null
                && event.getScheduledPublishAt().isAfter(request.startsAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reschedule publication before moving the event start earlier.");
        }
        event.updateDetails(request);
        return toView(eventRepository.saveAndFlush(event), nowUtc());
    }

    public EventView schedule(UUID id, OffsetDateTime publishAt) {
        OffsetDateTime now = nowUtc();
        if (publishAt == null || !publishAt.isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scheduled publication must be in the future.");
        }
        requireIndiaStandardTime(publishAt);
        Event event = findById(id);
        if (!publishAt.isBefore(event.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publication must be scheduled before the event starts.");
        }
        event.schedule(publishAt);
        return toView(eventRepository.saveAndFlush(event), now);
    }

    public EventView publish(UUID id) {
        Event event = findById(id);
        event.publish();
        return toView(eventRepository.saveAndFlush(event), nowUtc());
    }

    public EventView close(UUID id) {
        Event event = findById(id);
        EventStatus status = effectiveStatus(event, nowUtc());
        if (status != EventStatus.PUBLISHED && status != EventStatus.CLOSED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only a published event can be closed.");
        }
        event.close();
        return toView(eventRepository.saveAndFlush(event), nowUtc());
    }

    @Transactional(readOnly = true)
    public List<EventView> listAdmin() {
        OffsetDateTime now = nowUtc();
        return eventRepository.findAllByOrderByStartsAtDesc().stream()
                .map(event -> toView(event, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicEventView> listPublic() {
        OffsetDateTime now = nowUtc();
        return eventRepository.findAllByOrderByStartsAtDesc().stream()
                .filter(event -> isPublic(event, now))
                .map(event -> toPublicView(event, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicEventView getPublic(UUID id) {
        OffsetDateTime now = nowUtc();
        Event event = findById(id);
        if (!isPublic(event, now)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.");
        }
        return toPublicView(event, now);
    }

    private Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
    }

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
    }

    private void validateRequest(EventRequest request) {
        requireIndiaStandardTime(request.startsAt());
        requireIndiaStandardTime(request.endsAt());
        requireIndiaStandardTime(request.registrationDeadline());
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The event end must be after its start.");
        }
        if (request.registrationDeadline().isAfter(request.startsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The registration deadline must be on or before the event starts.");
        }
        if (request.registrationType() == RegistrationType.TEAM) {
            if (request.minimumTeamSize() == null || request.maximumTeamSize() == null
                    || request.minimumTeamSize() < 1
                    || request.maximumTeamSize() < request.minimumTeamSize()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team events require valid minimum and maximum team sizes.");
            }
        } else if (request.minimumTeamSize() != null || request.maximumTeamSize() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team size limits apply only to team events.");
        }
        validateMediaUrl(request.posterUrl());
        if (request.archivePhotos() != null) {
            request.archivePhotos().forEach(this::validateMediaUrl);
        }
    }

    private void validateMediaUrl(String value) {
        if (value == null || value.isBlank()) return;
        try {
            URI uri = URI.create(value.trim());
            boolean localPath = uri.getScheme() == null && value.startsWith("/") && !value.startsWith("//");
            boolean secureUrl = "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
            if (!localPath && !secureUrl) throw invalidMediaUrl();
        } catch (IllegalArgumentException exception) {
            throw invalidMediaUrl();
        }
    }

    private void requireIndiaStandardTime(OffsetDateTime dateTime) {
        if (!ZoneOffset.ofHoursMinutes(5, 30).equals(dateTime.getOffset())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event dates and times must use India Standard Time (UTC+05:30).");
        }
    }

    private ResponseStatusException invalidMediaUrl() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Event images must use an HTTPS URL or a same-site absolute path.");
    }

    private boolean isPublic(Event event, OffsetDateTime now) {
        return switch (effectiveStatus(event, now)) {
            case PUBLISHED, CLOSED -> true;
            case DRAFT, SCHEDULED -> false;
        };
    }

    private EventStatus effectiveStatus(Event event, OffsetDateTime now) {
        if (event.getPublicationState() == EventStatus.SCHEDULED
                && event.getScheduledPublishAt() != null
                && !event.getScheduledPublishAt().isAfter(now)) {
            return EventStatus.PUBLISHED;
        }
        return event.getPublicationState();
    }

    private EventView toView(Event event, OffsetDateTime now) {
        EventStatus status = effectiveStatus(event, now);
        boolean ended = !event.getEndsAt().isAfter(now);
        int remainingCapacity = registrationCapacityService.remainingCapacity(
                event.getId(), event.getCapacity(), now);
        boolean registrationWindowOpen = status == EventStatus.PUBLISHED
                && !ended
                && event.getRegistrationDeadline().isAfter(now)
                && remainingCapacity > 0;
        return new EventView(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getVenue(),
                event.getPosterUrl(),
                event.getEligibility(),
                event.getFeeInr(),
                event.getRegistrationType(),
                event.getRegistrationDeadline(),
                event.getCapacity(),
                remainingCapacity,
                event.getMinimumTeamSize(),
                event.getMaximumTeamSize(),
                status,
                event.getScheduledPublishAt(),
                ended,
                registrationWindowOpen,
                event.getArchiveStory(),
                event.getArchiveResults(),
                event.getArchivePhotos(),
                event.getCreatedAt(),
                event.getUpdatedAt());
    }

    private PublicEventView toPublicView(Event event, OffsetDateTime now) {
        EventView view = toView(event, now);
        return new PublicEventView(
                view.id(),
                view.title(),
                view.description(),
                view.startsAt(),
                view.endsAt(),
                view.venue(),
                view.posterUrl(),
                view.eligibility(),
                view.feeInr(),
                view.registrationType(),
                view.registrationDeadline(),
                view.capacity(),
                view.remainingCapacity(),
                view.minimumTeamSize(),
                view.maximumTeamSize(),
                view.status(),
                view.ended(),
                view.registrationWindowOpen(),
                event.getFeeInr().signum() == 0 || paymentGateway.isCheckoutConfigured(),
                view.archiveStory(),
                view.archiveResults(),
                view.archivePhotos());
    }
}
