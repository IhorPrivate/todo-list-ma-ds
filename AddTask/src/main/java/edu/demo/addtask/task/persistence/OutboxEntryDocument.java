package edu.demo.addtask.task.persistence;

import java.time.Instant;

/**
 * An unpublished event embedded in its task document.
 *
 * @param eventId    event id as a string
 * @param routingKey routing key on {@code tasks.events}
 * @param payload    event JSON
 * @param occurredAt when the event happened
 */
record OutboxEntryDocument(String eventId, String routingKey, String payload, Instant occurredAt) {
}
