package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.notification.domain.ReadStatus;
import edu.demo.notificationservice.support.InMemoryNotificationRepository;
import edu.demo.notificationservice.support.NotificationFixtures;
import edu.demo.notificationservice.support.TestMessages;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static edu.demo.notificationservice.support.NotificationFixtures.CREATED_AT;
import static edu.demo.notificationservice.support.NotificationFixtures.READ_AT;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationReadingServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T15:00:00.000Z");
    private static final Instant NEWER = CREATED_AT.plusSeconds(60);
    private static final UUID UNKNOWN_ID = UUID.fromString("00000000-0000-4000-8000-000000000404");

    private final InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
    private final NotificationReadingService service =
            new NotificationReadingService(repository, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void markRead_givenUnknownId_returnsEmpty() {
        assertThat(service.markRead(UNKNOWN_ID)).isEmpty();
    }

    @Test
    void markRead_givenUnread_persistsReadTime() {
        var unread = NotificationFixtures.unread(new Audience.Everyone());
        repository.insertIfAbsent(unread);

        service.markRead(unread.id());

        assertThat(repository.findById(unread.id()))
                .map(Notification::readStatus)
                .contains(new ReadStatus.Read(NOW));
    }

    @Test
    void markRead_givenAlreadyRead_keepsFirstReadTime() {
        var read = NotificationFixtures.read(new Audience.Everyone());
        repository.insertIfAbsent(read);

        var result = service.markRead(read.id());

        assertThat(result)
                .as(TestMessages.get("read.first-read-time-kept"))
                .map(Notification::readStatus)
                .contains(new ReadStatus.Read(READ_AT));
    }

    @Test
    void find_givenSeveralNotifications_returnsNewestFirst() {
        var older = NotificationFixtures.created(new Audience.Everyone(), CREATED_AT);
        var newer = NotificationFixtures.created(new Audience.Everyone(), NEWER);
        repository.insertIfAbsent(older);
        repository.insertIfAbsent(newer);

        assertThat(service.find(NotificationFilter.all())).containsExactly(newer, older);
    }
}
