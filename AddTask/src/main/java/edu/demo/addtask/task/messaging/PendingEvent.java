package edu.demo.addtask.task.messaging;

import java.util.UUID;

/**
 * An event stored with its task and not yet confirmed by the broker.
 *
 * @param taskId     the task document holding the event
 * @param eventId    the event's id, sent as the AMQP message id
 * @param routingKey where to publish on {@code tasks.events}
 * @param payload    the event as JSON
 */
public record PendingEvent(UUID taskId, UUID eventId, String routingKey, String payload) {
}
