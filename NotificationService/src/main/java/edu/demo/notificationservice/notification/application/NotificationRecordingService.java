package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationPolicy;
import edu.demo.notificationservice.notification.domain.PlannedNotification;
import edu.demo.notificationservice.notification.domain.TaskActivity;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * Generates notifications from task activity, stores each one once and delivers it when first stored.
 *
 * @since 0.0.1
 */
@Service
public class NotificationRecordingService implements TaskActivityRecorder {

    private final NotificationPolicy policy;
    private final NotificationRepository repository;
    private final NotificationChannel channel;
    private final MessageCatalog catalog;
    private final Clock clock;

    /**
     * @param policy     decides which notifications an activity produces
     * @param repository notification storage
     * @param channel    output for newly stored notifications
     * @param catalog    renders notification text
     * @param clock      source of creation times
     */
    public NotificationRecordingService(
            NotificationPolicy policy,
            NotificationRepository repository,
            NotificationChannel channel,
            MessageCatalog catalog,
            Clock clock) {
        this.policy = policy;
        this.repository = repository;
        this.channel = channel;
        this.catalog = catalog;
        this.clock = clock;
    }

    @Override
    public void record(TaskActivity activity) {
        for (var planned : policy.plan(activity)) {
            var notification = materialise(planned);
            // A redelivered event maps to the same ids; only a first insert is new and gets delivered.
            if (repository.insertIfAbsent(notification)) {
                channel.deliver(notification);
            }
        }
    }

    private Notification materialise(PlannedNotification planned) {
        return planned.toNotification(catalog.render(planned.type(), planned.messageArguments()), clock.instant());
    }
}
