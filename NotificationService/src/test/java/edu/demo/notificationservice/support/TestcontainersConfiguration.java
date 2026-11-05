package edu.demo.notificationservice.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

/**
 * Real RabbitMQ and MongoDB for integration tests; {@code @ServiceConnection} wires their addresses into Boot.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final String RABBITMQ_IMAGE = "rabbitmq:4.2-management";
    private static final String MONGO_IMAGE = "mongo:8.0";

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitMQContainer() {
        return new RabbitMQContainer(RABBITMQ_IMAGE);
    }

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(MONGO_IMAGE);
    }
}
