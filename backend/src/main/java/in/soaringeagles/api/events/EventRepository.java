package in.soaringeagles.api.events;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findAllByOrderByStartsAtDesc();

    @Query("""
            select event.id as eventId, event.scheduledPublishAt as publishAt
            from Event event
            where event.publicationState = :publicationState
              and event.scheduledPublishAt is not null
            order by event.scheduledPublishAt asc
            """)
    List<ScheduledPublicationProjection> findScheduledPublications(
            @Param("publicationState") EventStatus publicationState);

    @Query("""
            select distinct event from Event event
            left join fetch event.archivePhotos
            order by event.startsAt desc
            """)
    List<Event> findAllWithArchivePhotosByOrderByStartsAtDesc();

    @Query("""
            select distinct event from Event event
            left join fetch event.archivePhotos
            where event.publicationState in (
                in.soaringeagles.api.events.EventStatus.PUBLISHED,
                in.soaringeagles.api.events.EventStatus.CLOSED)
              or (
                event.publicationState = in.soaringeagles.api.events.EventStatus.SCHEDULED
                and event.scheduledPublishAt <= :now
              )
            order by event.startsAt desc
            """)
    List<Event> findPublicEvents(@Param("now") OffsetDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from Event event where event.id = :id")
    java.util.Optional<Event> findByIdForUpdate(@Param("id") UUID id);

    interface ScheduledPublicationProjection {
        UUID getEventId();
        OffsetDateTime getPublishAt();
    }
}
