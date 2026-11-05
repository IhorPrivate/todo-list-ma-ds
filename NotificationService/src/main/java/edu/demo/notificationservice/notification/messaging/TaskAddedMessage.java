package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.TaskActivity;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire form of the {@code task.added} event published by the task service. {@code description} and
 * {@code assignee} are {@code null} when absent.
 */
record TaskAddedMessage(
        UUID eventId, Instant occurredAt, UUID taskId, String title, String description, String assignee) {

    private static final String EVENT_TYPE = "TaskAdded";

    TaskActivity.Added toActivity() {
        return new TaskActivity.Added(
                RequiredFields.require(eventId, EVENT_TYPE, "eventId"),
                RequiredFields.require(taskId, EVENT_TYPE, "taskId"),
                RequiredFields.require(title, EVENT_TYPE, "title"),
                audience());
    }

    private Audience audience() {
        return assignee == null || assignee.isBlank() ? new Audience.Everyone() : new Audience.Person(assignee);
    }
}
