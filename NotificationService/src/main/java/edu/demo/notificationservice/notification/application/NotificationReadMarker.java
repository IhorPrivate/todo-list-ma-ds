package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;

import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port: marks notifications read.
 *
 * @since 0.0.1
 */
public interface NotificationReadMarker {

    /**
     * Marks a notification read. Idempotent: the first read time is kept.
     *
     * @param id notification id
     * @return the notification as stored after the call, or {@link Optional#empty()} when unknown
     */
    Optional<Notification> markRead(UUID id);
}
