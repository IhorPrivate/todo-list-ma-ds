package edu.demo.notificationservice.notification.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A notification generated from task activity. Immutable; state changes return a new instance.
 *
 * @param id            deterministic identity derived from the source event, type and audience
 * @param type          what happened
 * @param audience      who the notification is for
 * @param message       human-readable text, rendered when the notification was generated
 * @param taskId        the task the notification is about
 * @param sourceEventId the task event that caused it
 * @param createdAt     when it was generated
 * @param readStatus    whether, and when, it was read
 * @since 0.0.1
 */
public record Notification(
        UUID id,
        NotificationType type,
        Audience audience,
        String message,
        UUID taskId,
        UUID sourceEventId,
        Instant createdAt,
        ReadStatus readStatus) {

    /**
     * Marks the notification read. Idempotent: an already-read notification keeps its first read time.
     *
     * @param at the moment of reading
     * @return this notification if already read, otherwise a copy marked read at {@code at}
     */
    public Notification markRead(Instant at) {
        return switch (readStatus) {
            case ReadStatus.Read _ -> this;
            case ReadStatus.Unread _ -> new Notification(
                    id, type, audience, message, taskId, sourceEventId, createdAt, new ReadStatus.Read(at));
        };
    }

    /**
     * @return {@code true} once the notification has been marked read
     */
    public boolean isRead() {
        return readStatus instanceof ReadStatus.Read;
    }
}
