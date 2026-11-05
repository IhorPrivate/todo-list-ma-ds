package edu.demo.addtask.task.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Identity and time shared by every event.
 *
 * @param eventId    unique per event; consumers deduplicate on it
 * @param occurredAt when the change happened
 * @since 0.1.0
 */
public record EventStamp(UUID eventId, Instant occurredAt) {

    public EventStamp {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
