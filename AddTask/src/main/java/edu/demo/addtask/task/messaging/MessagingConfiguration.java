package edu.demo.addtask.task.messaging;

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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Broker topology, JSON conversion and retry policy.
 */
@Configuration
@EnableConfigurationProperties(OutboxRelaySettings.class)
class MessagingConfiguration {

    @Bean
    MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    RabbitListenerRetrySettingsCustomizer skipRetryForRejectedMessages() {
        return settings -> settings.setExceptionPredicate(RetryClassification::isRetryable);
    }

    @Bean
    Declarables tasksTopology() {
        DirectExchange commands = ExchangeBuilder.directExchange(TasksTopology.COMMANDS_EXCHANGE).durable(true).build();
        TopicExchange events = ExchangeBuilder.topicExchange(TasksTopology.EVENTS_EXCHANGE).durable(true).build();
        DirectExchange deadLetters = ExchangeBuilder.directExchange(TasksTopology.DEAD_LETTER_EXCHANGE)
                .durable(true).build();

        var addTask = workQueue(TasksTopology.ADD_TASK_QUEUE);
        var assignTask = workQueue(TasksTopology.ASSIGN_TASK_QUEUE);
        var addTaskDlq = QueueBuilder.durable(TasksTopology.ADD_TASK_DLQ).quorum().build();
        var assignTaskDlq = QueueBuilder.durable(TasksTopology.ASSIGN_TASK_DLQ).quorum().build();

        return new Declarables(
                commands, events, deadLetters, addTask, assignTask, addTaskDlq, assignTaskDlq,
                BindingBuilder.bind(addTask).to(commands).with(TasksTopology.ADD_TASK_KEY),
                BindingBuilder.bind(assignTask).to(commands).with(TasksTopology.ASSIGN_TASK_KEY),
                BindingBuilder.bind(addTaskDlq).to(deadLetters).with(TasksTopology.ADD_TASK_QUEUE),
                BindingBuilder.bind(assignTaskDlq).to(deadLetters).with(TasksTopology.ASSIGN_TASK_QUEUE));
    }

    private static Queue workQueue(String name) {
        return QueueBuilder.durable(name)
                .quorum()
                .deadLetterExchange(TasksTopology.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(name)
                .build();
    }
}
