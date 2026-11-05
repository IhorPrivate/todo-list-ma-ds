package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serves notification reads and read-state changes.
 *
 * @since 0.0.1
 */
@Service
public class NotificationReadingService implements NotificationQueries, NotificationReadMarker {

    private final NotificationRepository repository;
    private final Clock clock;

    /**
     * @param repository notification storage
     * @param clock      source of read times
     */
    public NotificationReadingService(NotificationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public List<Notification> find(NotificationFilter filter) {
        return repository.findNewestFirst(filter);
    }

    @Override
    public Optional<Notification> byId(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Notification> markRead(UUID id) {
        return repository.findById(id).map(this::markRead);
    }

    private Notification markRead(Notification notification) {
        if (notification.isRead()) {
            return notification;
        }
        var read = notification.markRead(clock.instant());
        repository.replace(read);
        return read;
    }
}
