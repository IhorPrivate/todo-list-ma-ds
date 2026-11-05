package edu.demo.notificationservice.notification.domain;

import java.util.UUID;

/**
 * Something that happened to a task in the task service, as far as notifications care.
 *
 * @since 0.0.1
 */
public sealed interface TaskActivity permits TaskActivity.Added, TaskActivity.Assigned, TaskActivity.Reassigned {

    /**
     * @return the id of the event that reported this activity; the idempotency key
     */
    UUID eventId();

    /**
     * @return the task the activity concerns
     */
    UUID taskId();

    /**
     * @return the task title at the time of the activity
     */
    String title();

    /**
     * A task was added.
     *
     * @param eventId  source event id
     * @param taskId   task id
     * @param title    task title
     * @param audience the assignee, or everyone when the task was added unassigned
     */
    record Added(UUID eventId, UUID taskId, String title, Audience audience) implements TaskActivity {
    }

    /**
     * A task with no previous assignee was assigned.
     *
     * @param eventId  source event id
     * @param taskId   task id
     * @param title    task title
     * @param assignee the new assignee
     */
    record Assigned(UUID eventId, UUID taskId, String title, String assignee) implements TaskActivity {
    }

    /**
     * A task that had an assignee was assigned again, possibly to the same person.
     *
     * @param eventId          source event id
     * @param taskId           task id
     * @param title            task title
     * @param assignee         the new assignee
     * @param previousAssignee the assignee before this change
     */
    record Reassigned(UUID eventId, UUID taskId, String title, String assignee, String previousAssignee)
            implements TaskActivity {
    }
}
