package edu.demo.addtask.task.event;

import edu.demo.addtask.task.domain.Task;

import java.time.Instant;
import java.util.UUID;

/**
 * A task was added. Published with routing key {@code task.added}.
 *
 * <p>{@code description} and {@code assignee} are {@code null} on the wire when absent; this
 * record mirrors the published JSON contract.
 *
 * @param eventId     unique event id
 * @param occurredAt  when the task was added
 * @param taskId      the new task
 * @param title       task title
 * @param description task description, {@code null} when absent
 * @param assignee    assignee set on creation, {@code null} when unassigned
 * @since 0.1.0
 */
public record TaskAdded(UUID eventId, Instant occurredAt, UUID taskId, String title, String description,
                        String assignee) implements TaskEvent {

    /**
     * @param task  the task that was added
     * @param stamp event identity and time
     * @return the event describing the addition
     */
    public static TaskAdded of(Task task, EventStamp stamp) {
        return new TaskAdded(stamp.eventId(), stamp.occurredAt(), task.id(), task.title(),
                task.description().isEmpty() ? null : task.description(),
                task.assignee().orElse(null));
    }
}
