package edu.demo.addtask.task.event;

import edu.demo.addtask.task.domain.Task;

import java.time.Instant;
import java.util.UUID;

/**
 * A task got a new assignee. Published with routing key {@code task.assigned}.
 *
 * @param eventId          unique event id
 * @param occurredAt       when the assignment happened
 * @param taskId           the task
 * @param title            task title
 * @param assignee         the new assignee
 * @param previousAssignee the assignee before, {@code null} when it was unassigned
 * @since 0.1.0
 */
public record TaskAssigned(UUID eventId, Instant occurredAt, UUID taskId, String title, String assignee,
                           String previousAssignee) implements TaskEvent {

    /**
     * @param before the task before the assignment
     * @param after  the task after the assignment; must be assigned
     * @param stamp  event identity and time
     * @return the event describing the change
     */
    public static TaskAssigned of(Task before, Task after, EventStamp stamp) {
        return new TaskAssigned(stamp.eventId(), stamp.occurredAt(), after.id(), after.title(),
                after.assignee().orElseThrow(), before.assignee().orElse(null));
    }
}
