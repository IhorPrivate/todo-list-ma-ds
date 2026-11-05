package edu.demo.notificationservice.notification.domain;

import java.time.Instant;

/**
 * Whether a notification has been read, and when.
 *
 * @since 0.0.1
 */
public sealed interface ReadStatus permits ReadStatus.Unread, ReadStatus.Read {

    /**
     * The notification has not been read yet.
     */
    record Unread() implements ReadStatus {
    }

    /**
     * The notification was first read at the given instant.
     *
     * @param at when the notification was first marked read
     */
    record Read(Instant at) implements ReadStatus {
    }
}
