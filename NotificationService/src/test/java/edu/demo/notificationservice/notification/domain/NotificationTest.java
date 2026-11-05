package edu.demo.notificationservice.notification.domain;

import edu.demo.notificationservice.support.NotificationFixtures;
import edu.demo.notificationservice.support.TestMessages;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static edu.demo.notificationservice.support.NotificationFixtures.READ_AT;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private static final Instant LATER_READ = Instant.parse("2026-11-04T10:00:00.000Z");

    @Test
    void markRead_givenUnread_recordsReadTime() {
        var read = NotificationFixtures.unread(new Audience.Everyone()).markRead(READ_AT);

        assertThat(read.readStatus()).isEqualTo(new ReadStatus.Read(READ_AT));
    }

    @Test
    void markRead_givenAlreadyRead_keepsFirstReadTime() {
        var readAgain = NotificationFixtures.read(new Audience.Everyone()).markRead(LATER_READ);

        assertThat(readAgain.readStatus())
                .as(TestMessages.get("read.first-read-time-kept"))
                .isEqualTo(new ReadStatus.Read(READ_AT));
    }
}
