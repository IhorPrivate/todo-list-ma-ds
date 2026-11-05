package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.NotificationType;

import java.util.List;

/**
 * Outbound port: renders notification text.
 *
 * @since 0.0.1
 */
public interface MessageCatalog {

    /**
     * @param type      the notification type, which selects the message template
     * @param arguments template arguments in order
     * @return the rendered text
     */
    String render(NotificationType type, List<String> arguments);
}
