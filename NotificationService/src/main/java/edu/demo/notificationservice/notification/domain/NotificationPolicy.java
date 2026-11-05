package edu.demo.notificationservice.notification.domain;

import java.util.List;

/**
 * Decides which notifications a task activity produces.
 *
 * <p>Stateless and thread-safe.
 *
 * @since 0.0.1
 */
public final class NotificationPolicy {

    /**
     * Plans the notifications for one activity.
     *
     * @param activity what happened to the task
     * @return the notifications to generate, never empty
     */
    public List<PlannedNotification> plan(TaskActivity activity) {
        return switch (activity) {
            case TaskActivity.Added added -> List.of(
                    new PlannedNotification(added, NotificationType.TASK_ADDED, added.audience(), List.of(added.title())));
            case TaskActivity.Assigned assigned -> List.of(assignedTo(assigned, assigned.assignee()));
            case TaskActivity.Reassigned reassigned -> reassignment(reassigned);
        };
    }

    private static List<PlannedNotification> reassignment(TaskActivity.Reassigned reassigned) {
        var toNewAssignee = assignedTo(reassigned, reassigned.assignee());
        if (reassigned.previousAssignee().equals(reassigned.assignee())) {
            return List.of(toNewAssignee);
        }
        var toPreviousAssignee = new PlannedNotification(
                reassigned,
                NotificationType.TASK_UNASSIGNED,
                new Audience.Person(reassigned.previousAssignee()),
                List.of(reassigned.title(), reassigned.assignee()));
        return List.of(toNewAssignee, toPreviousAssignee);
    }

    private static PlannedNotification assignedTo(TaskActivity activity, String assignee) {
        return new PlannedNotification(
                activity, NotificationType.TASK_ASSIGNED, new Audience.Person(assignee), List.of(activity.title()));
    }
}
