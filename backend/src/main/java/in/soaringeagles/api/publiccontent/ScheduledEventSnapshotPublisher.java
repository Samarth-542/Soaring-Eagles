package in.soaringeagles.api.publiccontent;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import in.soaringeagles.api.events.EventService;
import in.soaringeagles.api.events.EventStatus;
import in.soaringeagles.api.events.EventView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ScheduledEventSnapshotPublisher {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledEventSnapshotPublisher.class);

    private final TaskScheduler taskScheduler;
    private final EventService eventService;
    private final PublicContentSnapshotPublisher snapshotPublisher;
    private final Clock clock;
    private final Map<UUID, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>();

    ScheduledEventSnapshotPublisher(
            TaskScheduler taskScheduler,
            EventService eventService,
            PublicContentSnapshotPublisher snapshotPublisher,
            Clock clock) {
        this.taskScheduler = taskScheduler;
        this.eventService = eventService;
        this.snapshotPublisher = snapshotPublisher;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void restoreScheduledPublications() {
        try {
            eventService.listScheduledPublications()
                    .forEach(event -> schedule(event.eventId(), event.publishAt()));
        } catch (DataAccessException exception) {
            logger.error("Scheduled event snapshot publications could not be restored from the database.", exception);
        }
    }

    public void syncAfterMutation(EventView event) {
        afterCommit(() -> {
            if (event.status() == EventStatus.SCHEDULED && event.scheduledPublishAt() != null) {
                schedule(event.id(), event.scheduledPublishAt());
            } else {
                cancel(event.id());
            }
        });
    }

    public void cancelAfterMutation(UUID eventId) {
        afterCommit(() -> cancel(eventId));
    }

    private void schedule(UUID eventId, java.time.OffsetDateTime publishAt) {
        cancel(eventId);
        ScheduledTask task = new ScheduledTask();
        Instant executionTime = publishAt.toInstant();
        if (!executionTime.isAfter(clock.instant())) {
            executionTime = clock.instant().plusSeconds(1);
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> publishIfPublic(eventId, task),
                executionTime);
        if (future == null) {
            logger.error("Scheduled event snapshot task was not accepted for event {}.", eventId);
            return;
        }
        task.future = future;
        scheduledTasks.put(eventId, task);
    }

    private void publishIfPublic(UUID eventId, ScheduledTask task) {
        if (!scheduledTasks.remove(eventId, task)) return;

        try {
            eventService.getPublic(eventId);
            snapshotPublisher.publishAfterMutation();
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.debug("Skipping scheduled snapshot publish because event {} is no longer public.", eventId);
            } else {
                logger.error("Scheduled snapshot publish could not verify event {}.", eventId, exception);
            }
        } catch (DataAccessException exception) {
            logger.error("Scheduled snapshot publish could not read event {}.", eventId, exception);
        }
    }

    private void cancel(UUID eventId) {
        ScheduledTask task = scheduledTasks.remove(eventId);
        if (task != null) task.future.cancel(false);
    }

    private void afterCommit(Runnable action) {
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

    private static final class ScheduledTask {
        private ScheduledFuture<?> future;
    }
}
