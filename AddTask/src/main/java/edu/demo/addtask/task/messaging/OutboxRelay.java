package edu.demo.addtask.task.messaging;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publishes events from the outbox and removes each one after the broker confirms it.
 *
 * <p>Delivery is at least once: a crash between confirm and removal republishes the event, so
 * consumers deduplicate on the event id. A failed publish stops the run; the event stays pending and
 * the next run retries it.
 */
@Component
class OutboxRelay {

    private final PendingEvents pendingEvents;
    private final EventPublisher publisher;
    private final OutboxRelaySettings settings;

    OutboxRelay(PendingEvents pendingEvents, EventPublisher publisher, OutboxRelaySettings settings) {
        this.pendingEvents = pendingEvents;
        this.publisher = publisher;
        this.settings = settings;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.interval-ms}")
    void relay() {
        pendingEvents.oldest(settings.batchSize()).forEach(event -> {
            publisher.publish(event);
            pendingEvents.markPublished(event);
        });
    }
}
