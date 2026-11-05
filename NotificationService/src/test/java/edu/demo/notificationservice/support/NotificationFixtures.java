package edu.demo.notificationservice.support;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationType;
import edu.demo.notificationservice.notification.domain.ReadStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Notifications in known states for service, web and persistence tests.
 */
public final class NotificationFixtures {

    public static final Instant CREATED_AT = Instant.parse("2026-11-03T09:00:00.000Z");
    public static final Instant READ_AT = Instant.parse("2026-11-03T09:30:00.000Z");
    public static final String MESSAGE = "Rendered notification text";

    private NotificationFixtures() {
    }

    public static Notification unread(Audience audience) {
        return created(audience, CREATED_AT);
    }

    public static Notification created(Audience audience, Instant createdAt) {
        return new Notification(
                UUID.randomUUID(),
                NotificationType.TASK_ADDED,
                audience,
                MESSAGE,
                TaskActivityFixtures.TASK_ID,
                TaskActivityFixtures.EVENT_ID,
                createdAt,
                new ReadStatus.Unread());
    }

    public static Notification read(Audience audience) {
        return unread(audience).markRead(READ_AT);
    }
}
