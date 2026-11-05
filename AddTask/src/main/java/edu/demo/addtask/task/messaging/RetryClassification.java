package edu.demo.addtask.task.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Decides which listener failures are worth retrying.
 */
final class RetryClassification {

    private RetryClassification() {
    }

    /**
     * @param failure what the listener threw, usually wrapped in a
     *                {@code ListenerExecutionFailedException}
     * @return {@code false} when any cause asks to reject without requeue
     */
    static boolean isRetryable(Throwable failure) {
        for (var cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof AmqpRejectAndDontRequeueException) {
                return false;
            }
        }
        return true;
    }
}
