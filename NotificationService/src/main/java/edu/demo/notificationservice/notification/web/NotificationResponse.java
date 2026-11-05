package edu.demo.notificationservice.notification.web;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationType;
import edu.demo.notificationservice.notification.domain.ReadStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * JSON representation of a notification. {@code recipient} is {@code null} for broadcasts and {@code readAt} is
 * {@code null} while unread.
 */
record NotificationResponse(
        UUID id,
        NotificationType type,
        String recipient,
        String message,
        UUID taskId,
        Instant createdAt,
        Instant readAt) {

    static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.type(),
                switch (notification.audience()) {
                    case Audience.Everyone _ -> null;
                    case Audience.Person person -> person.name();
                },
                notification.message(),
                notification.taskId(),
                notification.createdAt(),
                switch (notification.readStatus()) {
                    case ReadStatus.Unread _ -> null;
                    case ReadStatus.Read read -> read.at();
                });
    }
}
