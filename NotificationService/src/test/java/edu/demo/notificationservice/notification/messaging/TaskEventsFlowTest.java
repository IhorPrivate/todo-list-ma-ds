package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.notification.application.NotificationQueries;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.support.Fixtures;
import edu.demo.notificationservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end: events published to {@code tasks.events} become stored notifications; poison messages land in
 * the dead-letter queue. Skipped when Docker is unavailable.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class TaskEventsFlowTest {

    private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration SETTLE_TIME = Duration.ofSeconds(2);
    private static final String TASK_ADDED = "events/task-added.json";
    private static final String TASK_REASSIGNED = "events/task-reassigned.json";
    private static final String TASK_ADDED_MISSING_TASK_ID = "events/task-added-missing-task-id.json";
    private static final String NOT_JSON = "events/not-json.txt";
    private static final int ONE_NOTIFICATION = 1;
    private static final int NOTIFICATIONS_PER_REASSIGNMENT = 2;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationQueries queries;

    @Autowired
    private MongoOperations mongo;

    @BeforeEach
    void emptyDatabase() {
        mongo.getCollectionNames().forEach(mongo::dropCollection);
    }

    @Test
    void onTaskAdded_givenPublishedEvent_storesNotification() {
        publish(TaskEventsTopology.TASK_ADDED_KEY, TASK_ADDED);

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(queries.find(NotificationFilter.all())).hasSize(ONE_NOTIFICATION));
    }

    @Test
    void onTaskAssigned_givenReassignment_storesBothNotifications() {
        publish(TaskEventsTopology.TASK_ASSIGNED_KEY, TASK_REASSIGNED);

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(queries.find(NotificationFilter.all())).hasSize(NOTIFICATIONS_PER_REASSIGNMENT));
    }

    @Test
    void onTaskAdded_givenRedeliveredEvent_storesOneNotification() {
        publish(TaskEventsTopology.TASK_ADDED_KEY, TASK_ADDED);
        publish(TaskEventsTopology.TASK_ADDED_KEY, TASK_ADDED);

        await().during(SETTLE_TIME).atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(queries.find(NotificationFilter.all())).hasSize(ONE_NOTIFICATION));
    }

    @Test
    void onTaskAdded_givenMalformedEvent_landsInDeadLetterQueue() {
        publish(TaskEventsTopology.TASK_ADDED_KEY, TASK_ADDED_MISSING_TASK_ID);

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(rabbitTemplate.receive(TaskEventsTopology.TASK_ADDED_DLQ)).isNotNull());
    }

    @Test
    void onTaskAdded_givenUnparseableMessage_landsInDeadLetterQueue() {
        publish(TaskEventsTopology.TASK_ADDED_KEY, NOT_JSON);

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(rabbitTemplate.receive(TaskEventsTopology.TASK_ADDED_DLQ)).isNotNull());
    }

    private void publish(String routingKey, String fixture) {
        Message message = MessageBuilder.withBody(Fixtures.read(fixture).getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        rabbitTemplate.send(TaskEventsTopology.EVENTS_EXCHANGE, routingKey, message);
    }
}
