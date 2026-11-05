package edu.demo.addtask.task.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Tuning for the outbox relay ({@code outbox.relay.*}).
 *
 * @param intervalMs     pause between relay runs
 * @param batchSize      most events published per run
 * @param confirmTimeout how long to wait for the broker to confirm one event
 */
@ConfigurationProperties("outbox.relay")
record OutboxRelaySettings(long intervalMs, int batchSize, Duration confirmTimeout) {
}
