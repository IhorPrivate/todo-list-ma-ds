package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.support.TestMessages;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.listener.ListenerExecutionFailedException;

import static org.assertj.core.api.Assertions.assertThat;

class RetryClassificationTest {

    private static final String EVENT_TYPE = "TaskAdded";
    private static final String MISSING_FIELD = "taskId";
    private static final String LISTENER_FAILED = "Listener threw exception";
    private static final String BROKER_HICCUP = "Mongo unavailable";

    @Test
    void isRetryable_givenTransientFailure_returnsTrue() {
        var failure = new ListenerExecutionFailedException(LISTENER_FAILED, new IllegalStateException(BROKER_HICCUP));

        assertThat(RetryClassification.isRetryable(failure)).isTrue();
    }

    @Test
    void isRetryable_givenWrappedRejection_returnsFalse() {
        var failure = new ListenerExecutionFailedException(
                LISTENER_FAILED, new MalformedEventException(EVENT_TYPE, MISSING_FIELD));

        assertThat(RetryClassification.isRetryable(failure))
                .as(TestMessages.get("retry.rejected-messages-skip-retries"))
                .isFalse();
    }

    @Test
    void isRetryable_givenDirectRejection_returnsFalse() {
        assertThat(RetryClassification.isRetryable(new MalformedEventException(EVENT_TYPE, MISSING_FIELD)))
                .as(TestMessages.get("retry.rejected-messages-skip-retries"))
                .isFalse();
    }
}
