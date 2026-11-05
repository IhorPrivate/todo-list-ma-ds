package edu.demo.notificationservice.notification.application;

import java.util.UUID;

/**
 * Thrown when a requested notification does not exist.
 *
 * @since 0.0.1
 */
public final class NotificationNotFoundException extends RuntimeException {

    /**
     * @param id the unknown notification id
     */
    public NotificationNotFoundException(UUID id) {
        super("Notification %s was not found".formatted(id));
    }
}
