package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.notification.domain.TaskActivity;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire form of the {@code task.assigned} event published by the task service. {@code previousAssignee} is
 * {@code null} when the task had no assignee before.
 */
record TaskAssignedMessage(
        UUID eventId, Instant occurredAt, UUID taskId, String title, String assignee, String previousAssignee) {

    private static final String EVENT_TYPE = "TaskAssigned";

    TaskActivity toActivity() {
        var id = RequiredFields.require(eventId, EVENT_TYPE, "eventId");
        var task = RequiredFields.require(taskId, EVENT_TYPE, "taskId");
        var taskTitle = RequiredFields.require(title, EVENT_TYPE, "title");
        var newAssignee = RequiredFields.require(assignee, EVENT_TYPE, "assignee");
        if (previousAssignee == null || previousAssignee.isBlank()) {
            return new TaskActivity.Assigned(id, task, taskTitle, newAssignee);
        }
        return new TaskActivity.Reassigned(id, task, taskTitle, newAssignee, previousAssignee);
    }
}
