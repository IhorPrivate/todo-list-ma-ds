package edu.demo.notificationservice.notification.messaging;

/**
 * Names of every exchange, queue, routing key and listener this service uses. Shared by declarations, listeners
 * and tests so a rename is a one-place edit.
 */
final class TaskEventsTopology {

    static final String EVENTS_EXCHANGE = "tasks.events";
    static final String DEAD_LETTER_EXCHANGE = "notifications.dead-letters";

    static final String TASK_ADDED_KEY = "task.added";
    static final String TASK_ASSIGNED_KEY = "task.assigned";

    static final String TASK_ADDED_QUEUE = "notifications.task-added";
    static final String TASK_ASSIGNED_QUEUE = "notifications.task-assigned";

    private static final String DLQ_SUFFIX = ".dlq";
    static final String TASK_ADDED_DLQ = TASK_ADDED_QUEUE + DLQ_SUFFIX;
    static final String TASK_ASSIGNED_DLQ = TASK_ASSIGNED_QUEUE + DLQ_SUFFIX;

    static final String TASK_ADDED_LISTENER = "notifications.task-added-listener";
    static final String TASK_ASSIGNED_LISTENER = "notifications.task-assigned-listener";

    private TaskEventsTopology() {
    }
}
