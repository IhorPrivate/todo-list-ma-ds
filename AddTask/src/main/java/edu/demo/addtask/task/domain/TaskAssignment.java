package edu.demo.addtask.task.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A request to assign an existing task to a person.
 *
 * @param taskId   the task to assign
 * @param assignee the person, never blank
 * @since 0.1.0
 */
public record TaskAssignment(UUID taskId, String assignee) {

    public TaskAssignment {
        Objects.requireNonNull(taskId, "taskId");
        if (assignee == null || assignee.isBlank()) {
            throw new IllegalArgumentException("assignee must not be blank");
        }
        assignee = assignee.strip();
    }
}
