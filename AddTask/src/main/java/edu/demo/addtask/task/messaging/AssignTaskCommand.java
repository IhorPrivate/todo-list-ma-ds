package edu.demo.addtask.task.messaging;

import edu.demo.addtask.task.domain.TaskAssignment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Command on {@code tasks.commands} with key {@code task.assign}. Assigning to the current
 * assignee is a no-op, so redelivery is safe.
 *
 * @param commandId   unique per request; required
 * @param taskId      the task to assign; required
 * @param assignee    required, at most 100 characters
 * @param requestedAt when the sender issued the command
 */
public record AssignTaskCommand(
        @NotNull UUID commandId,
        @NotNull UUID taskId,
        @NotBlank @Size(max = 100) String assignee,
        Instant requestedAt) {

    TaskAssignment toAssignment() {
        return new TaskAssignment(taskId, assignee);
    }
}
