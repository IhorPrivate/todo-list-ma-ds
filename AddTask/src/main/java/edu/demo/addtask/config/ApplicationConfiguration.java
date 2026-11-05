package edu.demo.addtask.config;

import edu.demo.addtask.task.application.TaskIds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.UUID;

/**
 * Time, identity and scheduling infrastructure.
 */
@Configuration
@EnableScheduling
class ApplicationConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TaskIds taskIds() {
        return UUID::randomUUID;
    }
}
