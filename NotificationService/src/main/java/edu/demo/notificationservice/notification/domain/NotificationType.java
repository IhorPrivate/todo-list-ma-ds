package edu.demo.notificationservice.notification.domain;

/**
 * The kinds of notification this service generates.
 *
 * @since 0.0.1
 */
public enum NotificationType {

    /** A task was added; addressed to its assignee, or broadcast when unassigned. */
    TASK_ADDED,

    /** A task was assigned to the recipient. */
    TASK_ASSIGNED,

    /** A task the recipient held was reassigned to someone else. */
    TASK_UNASSIGNED
}
