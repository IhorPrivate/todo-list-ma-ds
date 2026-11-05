package edu.demo.notificationservice.support;

import edu.demo.notificationservice.notification.application.NotificationRepository;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory {@link NotificationRepository} with the port's contract, for service tests.
 */
public final class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<UUID, Notification> notifications = new LinkedHashMap<>();

    @Override
    public boolean insertIfAbsent(Notification notification) {
        return notifications.putIfAbsent(notification.id(), notification) == null;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(notifications.get(id));
    }

    @Override
    public List<Notification> findNewestFirst(NotificationFilter filter) {
        return notifications.values().stream()
                .filter(filter::matches)
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .toList();
    }

    @Override
    public void replace(Notification notification) {
        notifications.put(notification.id(), notification);
    }

    public List<Notification> all() {
        return new ArrayList<>(notifications.values());
    }
}
