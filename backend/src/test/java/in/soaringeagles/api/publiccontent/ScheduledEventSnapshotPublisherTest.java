package in.soaringeagles.api.publiccontent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import in.soaringeagles.api.events.EventService;
import in.soaringeagles.api.events.EventStatus;
import in.soaringeagles.api.events.EventView;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;

class ScheduledEventSnapshotPublisherTest {

    @Test
    void publishesSnapshotOnceAtScheduledPublicationTime() {
        TaskScheduler taskScheduler = mock(TaskScheduler.class);
        EventService eventService = mock(EventService.class);
        PublicContentSnapshotPublisher snapshotPublisher = mock(PublicContentSnapshotPublisher.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        AtomicReference<Runnable> scheduledAction = new AtomicReference<>();
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        OffsetDateTime publishAt = OffsetDateTime.ofInstant(now.plusSeconds(600), ZoneOffset.UTC);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(invocation -> {
            scheduledAction.set(invocation.getArgument(0));
            return future;
        });

        ScheduledEventSnapshotPublisher publisher = new ScheduledEventSnapshotPublisher(
                taskScheduler,
                eventService,
                snapshotPublisher,
                Clock.fixed(now, ZoneOffset.UTC));
        UUID eventId = UUID.randomUUID();

        publisher.syncAfterMutation(event(eventId, publishAt, EventStatus.SCHEDULED));
        verify(taskScheduler).schedule(any(Runnable.class), eq(publishAt.toInstant()));

        scheduledAction.get().run();

        verify(eventService).getPublic(eventId);
        verify(snapshotPublisher).publishAfterMutation();
    }

    @Test
    void ignoresOldScheduledActionAfterEventIsRescheduled() {
        TaskScheduler taskScheduler = mock(TaskScheduler.class);
        EventService eventService = mock(EventService.class);
        PublicContentSnapshotPublisher snapshotPublisher = mock(PublicContentSnapshotPublisher.class);
        ScheduledFuture<?> firstFuture = mock(ScheduledFuture.class);
        ScheduledFuture<?> secondFuture = mock(ScheduledFuture.class);
        AtomicReference<Runnable> firstAction = new AtomicReference<>();
        AtomicReference<Runnable> secondAction = new AtomicReference<>();
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class)))
                .thenAnswer(invocation -> {
                    firstAction.set(invocation.getArgument(0));
                    return firstFuture;
                })
                .thenAnswer(invocation -> {
                    secondAction.set(invocation.getArgument(0));
                    return secondFuture;
                });
        Instant now = Instant.parse("2026-11-10T12:00:00Z");
        ScheduledEventSnapshotPublisher publisher = new ScheduledEventSnapshotPublisher(
                taskScheduler,
                eventService,
                snapshotPublisher,
                Clock.fixed(now, ZoneOffset.UTC));
        UUID eventId = UUID.randomUUID();
        OffsetDateTime firstTime = OffsetDateTime.ofInstant(now.plusSeconds(600), ZoneOffset.UTC);
        OffsetDateTime secondTime = OffsetDateTime.ofInstant(now.plusSeconds(900), ZoneOffset.UTC);

        publisher.syncAfterMutation(event(eventId, firstTime, EventStatus.SCHEDULED));
        publisher.syncAfterMutation(event(eventId, secondTime, EventStatus.SCHEDULED));
        firstAction.get().run();

        verify(firstFuture).cancel(false);
        verify(eventService, never()).getPublic(eventId);
        verify(snapshotPublisher, never()).publishAfterMutation();
        secondAction.get().run();
        verify(eventService).getPublic(eventId);
        verify(snapshotPublisher).publishAfterMutation();
    }

    private EventView event(UUID id, OffsetDateTime publishAt, EventStatus status) {
        return new EventView(
                id,
                "Test event",
                "Description",
                publishAt.plusDays(1),
                publishAt.plusDays(1).plusHours(1),
                "Venue",
                null,
                "Open to students",
                null,
                null,
                null,
                null,
                null,
                publishAt.plusDays(1),
                100,
                null,
                null,
                null,
                status,
                publishAt,
                false,
                false,
                null,
                null,
                List.of(),
                null,
                null);
    }
}
