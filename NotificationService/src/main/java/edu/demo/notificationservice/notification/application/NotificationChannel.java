package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;

/**
 * Outbound port: outputs a newly generated notification to its recipient (log, e-mail, websocket, ...).
 *
 * <p>Delivery is best effort and happens once, right after the notification is first stored. The stored
 * notification, served over REST, is the source of truth.
 *
 * @since 0.0.1
 */
public interface NotificationChannel {

    /**
     * @param notification the notification to output
     */
    void deliver(Notification notification);
}
