package edu.demo.notificationservice.support;

import edu.demo.notificationservice.notification.application.NotificationChannel;
import edu.demo.notificationservice.notification.domain.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link NotificationChannel} that remembers what it delivered.
 */
public final class RecordingNotificationChannel implements NotificationChannel {

    private final List<Notification> delivered = new ArrayList<>();

    @Override
    public void deliver(Notification notification) {
        delivered.add(notification);
    }

    public List<Notification> delivered() {
        return List.copyOf(delivered);
    }
}
