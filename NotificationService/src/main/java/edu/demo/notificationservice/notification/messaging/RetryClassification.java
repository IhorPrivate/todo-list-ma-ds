package edu.demo.notificationservice.notification.messaging;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * Decides whether a listener failure is worth retrying.
 */
final class RetryClassification {

    private RetryClassification() {
    }

    /**
     * A failure is retryable unless any exception in its cause chain is an {@link AmqpRejectAndDontRequeueException}.
     * The listener's exception reaches the retry policy wrapped in a {@code ListenerExecutionFailedException}, so
     * the whole chain is inspected.
     *
     * @param failure the listener failure
     * @return {@code false} for messages that can never succeed
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
