package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.Notification;
import edu.demo.notificationservice.notification.domain.NotificationPolicy;
import edu.demo.notificationservice.notification.domain.NotificationType;
import edu.demo.notificationservice.support.InMemoryNotificationRepository;
import edu.demo.notificationservice.support.RecordingNotificationChannel;
import edu.demo.notificationservice.support.TestMessages;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static edu.demo.notificationservice.support.TaskActivityFixtures.PREVIOUS_ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.reassignedFrom;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationRecordingServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-03T13:00:00.000Z");
    private static final int NOTIFICATIONS_PER_REASSIGNMENT = 2;
    private static final MessageCatalog TYPE_NAME_CATALOG = (type, arguments) -> type.name();

    private final InMemoryNotificationRepository repository = new InMemoryNotificationRepository();
    private final RecordingNotificationChannel channel = new RecordingNotificationChannel();
    private final NotificationRecordingService service = new NotificationRecordingService(
            new NotificationPolicy(), repository, channel, TYPE_NAME_CATALOG, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void record_givenNewActivity_storesEveryPlannedNotification() {
        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(repository.all()).hasSize(NOTIFICATIONS_PER_REASSIGNMENT);
    }

    @Test
    void record_givenNewActivity_deliversWhatItStored() {
        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(channel.delivered()).containsExactlyInAnyOrderElementsOf(repository.all());
    }

    @Test
    void record_givenActivity_rendersTextAndStampsClockTime() {
        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(repository.all())
                .extracting(Notification::message, Notification::createdAt)
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple(NotificationType.TASK_ASSIGNED.name(), NOW),
                        org.assertj.core.api.Assertions.tuple(NotificationType.TASK_UNASSIGNED.name(), NOW));
    }

    @Test
    void record_givenRedeliveredActivity_storesNothingNew() {
        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));
        List<Notification> afterFirstDelivery = repository.all();

        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(repository.all())
                .as(TestMessages.get("idempotency.redelivery-stores-nothing-new"))
                .isEqualTo(afterFirstDelivery);
    }

    @Test
    void record_givenRedeliveredActivity_deliversNothingNew() {
        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        service.record(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(channel.delivered())
                .as(TestMessages.get("idempotency.redelivery-delivers-nothing-new"))
                .hasSize(NOTIFICATIONS_PER_REASSIGNMENT);
    }
}
