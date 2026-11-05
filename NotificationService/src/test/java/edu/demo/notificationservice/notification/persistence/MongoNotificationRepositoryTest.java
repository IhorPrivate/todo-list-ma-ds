package edu.demo.notificationservice.notification.persistence;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.notification.domain.ReadFilter;
import edu.demo.notificationservice.notification.domain.RecipientFilter;
import edu.demo.notificationservice.support.NotificationFixtures;
import edu.demo.notificationservice.support.TestMessages;
import edu.demo.notificationservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static edu.demo.notificationservice.support.NotificationFixtures.CREATED_AT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the Mongo query has the same semantics as {@link NotificationFilter#matches}. Skipped without Docker.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class MongoNotificationRepositoryTest {

    private static final String VIEWER = "ivan";
    private static final String SOMEONE_ELSE = "judy";
    private static final Instant NEWER = CREATED_AT.plusSeconds(60);

    @Autowired
    private MongoNotificationRepository repository;

    @Autowired
    private MongoOperations mongo;

    @BeforeEach
    void emptyDatabase() {
        mongo.getCollectionNames().forEach(mongo::dropCollection);
    }

    @Test
    void insertIfAbsent_givenSameNotificationTwice_reportsDuplicate() {
        var notification = NotificationFixtures.unread(new Audience.Everyone());
        repository.insertIfAbsent(notification);

        assertThat(repository.insertIfAbsent(notification)).isFalse();
    }

    @Test
    void findById_givenStoredNotification_roundTripsEveryField() {
        var notification = NotificationFixtures.read(new Audience.Person(VIEWER));
        repository.insertIfAbsent(notification);

        assertThat(repository.findById(notification.id())).contains(notification);
    }

    @Test
    void findNewestFirst_givenVisibleToFilter_includesOwnAndBroadcastNotifications() {
        var own = NotificationFixtures.unread(new Audience.Person(VIEWER));
        var broadcast = NotificationFixtures.unread(new Audience.Everyone());
        var others = NotificationFixtures.unread(new Audience.Person(SOMEONE_ELSE));
        repository.insertIfAbsent(own);
        repository.insertIfAbsent(broadcast);
        repository.insertIfAbsent(others);

        var visible = repository.findNewestFirst(
                new NotificationFilter(new RecipientFilter.VisibleTo(VIEWER), ReadFilter.ANY));

        assertThat(visible)
                .as(TestMessages.get("filter.visible-includes-broadcasts"))
                .containsExactlyInAnyOrder(own, broadcast);
    }

    @Test
    void findNewestFirst_givenUnreadFilter_excludesReadNotifications() {
        var unread = NotificationFixtures.unread(new Audience.Everyone());
        repository.insertIfAbsent(unread);
        repository.insertIfAbsent(NotificationFixtures.read(new Audience.Everyone()));

        var result = repository.findNewestFirst(
                new NotificationFilter(new RecipientFilter.AnyRecipient(), ReadFilter.UNREAD));

        assertThat(result).containsExactly(unread);
    }

    @Test
    void findNewestFirst_givenSeveralNotifications_ordersNewestFirst() {
        var older = NotificationFixtures.created(new Audience.Everyone(), CREATED_AT);
        var newer = NotificationFixtures.created(new Audience.Everyone(), NEWER);
        repository.insertIfAbsent(older);
        repository.insertIfAbsent(newer);

        assertThat(repository.findNewestFirst(NotificationFilter.all())).containsExactly(newer, older);
    }

    @Test
    void replace_givenReadState_persistsIt() {
        var unread = NotificationFixtures.unread(new Audience.Everyone());
        repository.insertIfAbsent(unread);
        var read = unread.markRead(NotificationFixtures.READ_AT);

        repository.replace(read);

        assertThat(repository.findById(unread.id())).contains(read);
    }
}
