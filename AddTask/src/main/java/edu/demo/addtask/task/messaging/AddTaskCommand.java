package edu.demo.addtask.task.messaging;

import edu.demo.addtask.task.domain.TaskDraft;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Command on {@code tasks.commands} with key {@code task.add}. The {@code commandId} becomes the
 * task id, so a redelivered command adds the task once.
 *
 * @param commandId   unique per request; required
 * @param title       required, at most 200 characters
 * @param description optional, at most 2000 characters
 * @param assignee    optional assignee, at most 100 characters
 * @param requestedAt when the sender issued the command
 */
public record AddTaskCommand(
        @NotNull UUID commandId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 100) String assignee,
        Instant requestedAt) {

    TaskDraft toDraft() {
        return new TaskDraft(title, description == null ? "" : description, assignee == null ? "" : assignee);
    }
}
