package edu.demo.addtask.task.messaging;

/**
 * The broker did not accept an event. The event stays in the outbox and is retried on the next run.
 */
class EventPublishException extends RuntimeException {

    EventPublishException(String message) {
        super(message);
    }

    EventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
