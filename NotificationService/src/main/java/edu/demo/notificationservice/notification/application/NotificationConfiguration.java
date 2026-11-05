package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.NotificationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

/**
 * Wires the framework-free domain objects and the time source.
 *
 * @since 0.0.1
 */
@Configuration(proxyBeanMethods = false)
public class NotificationConfiguration {

    private static final Duration STORAGE_PRECISION = Duration.ofMillis(1);

    /**
     * UTC clock ticking in milliseconds, the precision MongoDB stores dates with, so a stored and reloaded
     * notification equals the one generated.
     *
     * @return the application clock
     */
    @Bean
    Clock clock() {
        return Clock.tick(Clock.systemUTC(), STORAGE_PRECISION);
    }

    /**
     * @return the notification policy
     */
    @Bean
    NotificationPolicy notificationPolicy() {
        return new NotificationPolicy();
    }
}
