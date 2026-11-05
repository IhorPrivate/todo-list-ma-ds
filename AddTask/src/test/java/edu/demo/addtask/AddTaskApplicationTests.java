package edu.demo.addtask;

import edu.demo.addtask.task.application.TaskQueries;
import edu.demo.addtask.task.messaging.AddTaskCommand;
import edu.demo.addtask.task.messaging.TasksTopology;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end through a real broker and database. Skipped when Docker is unavailable.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class AddTaskApplicationTests {

    private static final String RABBITMQ_IMAGE = "rabbitmq:4.2-management";
    private static final String MONGO_IMAGE = "mongo:8.0";
    private static final String ALL_TASK_EVENTS = "task.#";
    private static final Duration DELIVERY_TIMEOUT = Duration.ofSeconds(10);
    private static final Instant REQUESTED = Instant.parse("2026-11-03T10:00:00Z");
    private static final String TITLE = "Write release notes";
    private static final String BLANK_TITLE = " ";
    private static final String ALICE = "alice";

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(RABBITMQ_IMAGE);

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer(MONGO_IMAGE);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private TaskQueries queries;

    @Test
    void addTaskCommand_givenValidCommand_storesTaskAndPublishesTaskAdded() {
        var eventQueue = new AnonymousQueue();
        amqpAdmin.declareQueue(eventQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(eventQueue)
                .to(new TopicExchange(TasksTopology.EVENTS_EXCHANGE)).with(ALL_TASK_EVENTS));
        var commandId = UUID.randomUUID();

        rabbitTemplate.convertAndSend(TasksTopology.COMMANDS_EXCHANGE, TasksTopology.ADD_TASK_KEY,
                new AddTaskCommand(commandId, TITLE, null, ALICE, REQUESTED));

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() -> assertThat(queries.byId(commandId)).isPresent());
        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() -> assertThat(rabbitTemplate.receive(eventQueue.getName())).isNotNull());
    }

    @Test
    void addTaskCommand_givenInvalidCommand_landsInDeadLetterQueue() {
        rabbitTemplate.convertAndSend(TasksTopology.COMMANDS_EXCHANGE, TasksTopology.ADD_TASK_KEY,
                new AddTaskCommand(UUID.randomUUID(), BLANK_TITLE, null, null, REQUESTED));

        await().atMost(DELIVERY_TIMEOUT).untilAsserted(() ->
                assertThat(rabbitTemplate.receive(TasksTopology.ADD_TASK_DLQ)).isNotNull());
    }
}
