package edu.demo.notificationservice.notification.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * A task event that parsed but lacks a required field. It can never succeed, so it is rejected to the
 * dead-letter queue without retries.
 */
final class MalformedEventException extends AmqpRejectAndDontRequeueException {

    private static final String MESSAGE_FORMAT = "%s event is missing required field '%s'";

    MalformedEventException(String eventType, String field) {
        super(MESSAGE_FORMAT.formatted(eventType, field));
    }
}
