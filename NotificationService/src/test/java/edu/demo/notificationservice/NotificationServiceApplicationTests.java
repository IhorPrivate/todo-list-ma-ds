package edu.demo.notificationservice;

import edu.demo.notificationservice.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full context against real RabbitMQ and MongoDB. Skipped when Docker is unavailable.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
