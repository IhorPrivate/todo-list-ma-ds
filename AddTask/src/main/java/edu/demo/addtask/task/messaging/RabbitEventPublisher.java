package edu.demo.addtask.task.messaging;

import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes outbox events with publisher confirms and mandatory routing.
 */
@Component
class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxRelaySettings settings;

    RabbitEventPublisher(RabbitTemplate rabbitTemplate, OutboxRelaySettings settings) {
        this.rabbitTemplate = rabbitTemplate;
        this.settings = settings;
    }

    @Override
    public void publish(PendingEvent event) {
        var message = MessageBuilder.withBody(event.payload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setMessageId(event.eventId().toString())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        var correlation = new CorrelationData(event.eventId().toString());
        rabbitTemplate.send(TasksTopology.EVENTS_EXCHANGE, event.routingKey(), message, correlation);
        awaitConfirm(event, correlation);
    }

    private void awaitConfirm(PendingEvent event, CorrelationData correlation) {
        try {
            var confirm = correlation.getFuture().get(settings.confirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (correlation.getReturned() != null) {
                throw new EventPublishException("Event " + event.eventId() + " was unroutable with key "
                        + event.routingKey());
            }
            if (!confirm.ack()) {
                throw new EventPublishException("Broker rejected event " + event.eventId() + ": " + confirm.reason());
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted waiting for confirm of " + event.eventId(), interrupted);
        } catch (ExecutionException | TimeoutException failure) {
            throw new EventPublishException("No confirm for event " + event.eventId(), failure);
        }
    }
}
