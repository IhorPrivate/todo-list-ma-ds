package edu.demo.notificationservice.notification.domain;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A notification the policy decided to send, before its text is rendered and its time is fixed.
 *
 * @param cause             the activity that triggered it
 * @param type              the notification type
 * @param audience          the recipient
 * @param messageArguments  values for the message template of {@code type}, in template order
 * @since 0.0.1
 */
public record PlannedNotification(
        TaskActivity cause, NotificationType type, Audience audience, List<String> messageArguments) {

    private static final String KEY_SEPARATOR = "|";

    /**
     * The notification id: a name-based UUID of the source event, type and audience, so a redelivered event
     * yields the same id and storage can reject the duplicate.
     *
     * @return the deterministic id
     */
    public UUID id() {
        var key = String.join(KEY_SEPARATOR, cause.eventId().toString(), type.name(), audience.key());
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Materialises the plan into an unread notification.
     *
     * @param message   the rendered text
     * @param createdAt the generation time
     * @return the notification
     */
    public Notification toNotification(String message, Instant createdAt) {
        return new Notification(
                id(), type, audience, message, cause.taskId(), cause.eventId(), createdAt, new ReadStatus.Unread());
    }
}
