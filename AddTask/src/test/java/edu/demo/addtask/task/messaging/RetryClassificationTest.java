package edu.demo.addtask.task.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.listener.ListenerExecutionFailedException;

import static org.assertj.core.api.Assertions.assertThat;

class RetryClassificationTest {

    private static final String REASON = "listener failed";

    @Test
    void isRetryable_givenWrappedRejection_isFalse() {
        var failure = new ListenerExecutionFailedException(REASON, new AmqpRejectAndDontRequeueException(REASON));

        assertThat(RetryClassification.isRetryable(failure)).isFalse();
    }

    @Test
    void isRetryable_givenOrdinaryFailure_isTrue() {
        var failure = new ListenerExecutionFailedException(REASON, new IllegalStateException(REASON));

        assertThat(RetryClassification.isRetryable(failure)).isTrue();
    }
}
