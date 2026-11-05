package edu.demo.notificationservice.notification.delivery;

import edu.demo.notificationservice.notification.application.NotificationChannel;
import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Outputs notifications as one structured INFO log line each. The first channel; e-mail or websocket channels
 * implement the same port.
 */
@Component
class LoggingNotificationChannel implements NotificationChannel {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingNotificationChannel.class);
    private static final String EVERYONE = "*";

    @Override
    public void deliver(Notification notification) {
        LOG.atInfo()
                .addKeyValue("notificationId", notification.id())
                .addKeyValue("type", notification.type())
                .addKeyValue("recipient", recipientOf(notification.audience()))
                .addKeyValue("taskId", notification.taskId())
                .log("Notification for {}: {}", recipientOf(notification.audience()), notification.message());
    }

    private static String recipientOf(Audience audience) {
        return switch (audience) {
            case Audience.Everyone _ -> EVERYONE;
            case Audience.Person person -> person.name();
        };
    }
}
