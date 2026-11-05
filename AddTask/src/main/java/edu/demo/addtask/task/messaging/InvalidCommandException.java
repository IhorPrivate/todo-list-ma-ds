package edu.demo.addtask.task.messaging;

import jakarta.validation.ConstraintViolation;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A command that can never succeed. Rejected without retry, so it goes to the dead-letter queue.
 */
public class InvalidCommandException extends AmqpRejectAndDontRequeueException {

    /**
     * @param violations why the command is invalid; never empty
     */
    public InvalidCommandException(Set<? extends ConstraintViolation<?>> violations) {
        super("Invalid command: " + violations.stream()
                .sorted(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", ")));
    }
}
