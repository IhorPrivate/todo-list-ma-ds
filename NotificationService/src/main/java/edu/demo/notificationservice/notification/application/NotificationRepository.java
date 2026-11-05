package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: notification storage.
 *
 * @since 0.0.1
 */
public interface NotificationRepository {

    /**
     * Stores a notification unless one with the same id already exists. Atomic per notification.
     *
     * @param notification the notification to store
     * @return {@code true} if it was stored, {@code false} if the id already existed
     */
    boolean insertIfAbsent(Notification notification);

    /**
     * @param id notification id
     * @return the stored notification, or {@link Optional#empty()}
     */
    Optional<Notification> findById(UUID id);

    /**
     * @param filter search criteria
     * @return matching notifications ordered by creation time, newest first
     */
    List<Notification> findNewestFirst(NotificationFilter filter);

    /**
     * Replaces a stored notification with a new state of the same id.
     *
     * @param notification the new state
     */
    void replace(Notification notification);
}
