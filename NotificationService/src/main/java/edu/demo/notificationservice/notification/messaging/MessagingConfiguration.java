package edu.demo.notificationservice.notification.messaging;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Declares the RabbitMQ topology this service consumes from and configures message conversion and retries.
 */
@Configuration(proxyBeanMethods = false)
class MessagingConfiguration {

    @Bean
    MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    /**
     * Boot's listener retry retries every exception by default; a rejected (poison) message goes straight to
     * its dead-letter queue instead.
     */
    @Bean
    RabbitListenerRetrySettingsCustomizer skipRetryForRejectedMessages() {
        return settings -> settings.setExceptionPredicate(RetryClassification::isRetryable);
    }

    @Bean
    Declarables taskEventsTopology() {
        TopicExchange events = ExchangeBuilder.topicExchange(TaskEventsTopology.EVENTS_EXCHANGE).durable(true).build();
        DirectExchange deadLetters =
                ExchangeBuilder.directExchange(TaskEventsTopology.DEAD_LETTER_EXCHANGE).durable(true).build();

        var taskAdded = workQueue(TaskEventsTopology.TASK_ADDED_QUEUE);
        var taskAddedDlq = QueueBuilder.durable(TaskEventsTopology.TASK_ADDED_DLQ).quorum().build();
        var taskAssigned = workQueue(TaskEventsTopology.TASK_ASSIGNED_QUEUE);
        var taskAssignedDlq = QueueBuilder.durable(TaskEventsTopology.TASK_ASSIGNED_DLQ).quorum().build();

        return new Declarables(
                events, deadLetters, taskAdded, taskAddedDlq, taskAssigned, taskAssignedDlq,
                BindingBuilder.bind(taskAdded).to(events).with(TaskEventsTopology.TASK_ADDED_KEY),
                BindingBuilder.bind(taskAssigned).to(events).with(TaskEventsTopology.TASK_ASSIGNED_KEY),
                BindingBuilder.bind(taskAddedDlq).to(deadLetters).with(TaskEventsTopology.TASK_ADDED_QUEUE),
                BindingBuilder.bind(taskAssignedDlq).to(deadLetters).with(TaskEventsTopology.TASK_ASSIGNED_QUEUE));
    }

    private static Queue workQueue(String name) {
        return QueueBuilder.durable(name)
                .quorum()
                .deadLetterExchange(TaskEventsTopology.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(name)
                .build();
    }
}
