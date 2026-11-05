package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port: reads notifications.
 *
 * @since 0.0.1
 */
public interface NotificationQueries {

    /**
     * @param filter search criteria
     * @return matching notifications, newest first; empty when none match
     */
    List<Notification> find(NotificationFilter filter);

    /**
     * @param id notification id
     * @return the notification, or {@link Optional#empty()} when unknown
     */
    Optional<Notification> byId(UUID id);
}
