package edu.demo.notificationservice.notification.persistence;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationType;
import edu.demo.notificationservice.notification.domain.ReadStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB shape of a notification. Ids are stored as strings because Boot 4 sets no default UUID representation.
 * A missing {@code recipient} means a broadcast; a missing {@code readAt} means unread.
 */
@Document(NotificationDocument.COLLECTION)
record NotificationDocument(
        @Id String id,
        String type,
        String recipient,
        String message,
        String taskId,
        String sourceEventId,
        Instant createdAt,
        Instant readAt) {

    static final String COLLECTION = "notifications";
    static final String RECIPIENT = "recipient";
    static final String CREATED_AT = "createdAt";
    static final String READ_AT = "readAt";

    static NotificationDocument from(Notification notification) {
        return new NotificationDocument(
                notification.id().toString(),
                notification.type().name(),
                recipientOf(notification.audience()),
                notification.message(),
                notification.taskId().toString(),
                notification.sourceEventId().toString(),
                notification.createdAt(),
                readAtOf(notification.readStatus()));
    }

    Notification toDomain() {
        return new Notification(
                UUID.fromString(id),
                NotificationType.valueOf(type),
                recipient == null ? new Audience.Everyone() : new Audience.Person(recipient),
                message,
                UUID.fromString(taskId),
                UUID.fromString(sourceEventId),
                createdAt,
                readAt == null ? new ReadStatus.Unread() : new ReadStatus.Read(readAt));
    }

    private static String recipientOf(Audience audience) {
        return switch (audience) {
            case Audience.Everyone _ -> null;
            case Audience.Person person -> person.name();
        };
    }

    private static Instant readAtOf(ReadStatus status) {
        return switch (status) {
            case ReadStatus.Unread _ -> null;
            case ReadStatus.Read read -> read.at();
        };
    }
}
