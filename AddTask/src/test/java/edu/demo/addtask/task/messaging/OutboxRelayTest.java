package edu.demo.addtask.task.messaging;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OutboxRelayTest {

    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final PendingEvent FIRST = new PendingEvent(TASK_ID,
            UUID.fromString("00000000-0000-0000-0000-0000000000e1"), TasksTopology.TASK_ADDED_KEY, "{}");
    private static final PendingEvent SECOND = new PendingEvent(TASK_ID,
            UUID.fromString("00000000-0000-0000-0000-0000000000e2"), TasksTopology.TASK_ASSIGNED_KEY, "{}");
    private static final OutboxRelaySettings SETTINGS = new OutboxRelaySettings(500, 10, Duration.ofSeconds(1));
    private static final String BROKER_DOWN = "broker down";

    @Test
    void relay_givenPendingEvents_publishesAndMarksEachInOrder() {
        var outbox = new RecordingOutbox(List.of(FIRST, SECOND));
        var published = new ArrayList<PendingEvent>();

        new OutboxRelay(outbox, published::add, SETTINGS).relay();

        assertThat(published).containsExactly(FIRST, SECOND);
        assertThat(outbox.marked).containsExactly(FIRST, SECOND);
    }

    @Test
    void relay_givenPublishFailure_leavesEventPending() {
        var outbox = new RecordingOutbox(List.of(FIRST, SECOND));
        EventPublisher failing = event -> {
            throw new EventPublishException(BROKER_DOWN);
        };

        assertThatExceptionOfType(EventPublishException.class)
                .isThrownBy(() -> new OutboxRelay(outbox, failing, SETTINGS).relay());
        assertThat(outbox.marked).isEmpty();
    }

    private static final class RecordingOutbox implements PendingEvents {

        private final List<PendingEvent> pending;
        private final List<PendingEvent> marked = new ArrayList<>();

        private RecordingOutbox(List<PendingEvent> pending) {
            this.pending = pending;
        }

        @Override
        public List<PendingEvent> oldest(int limit) {
            return pending.stream().limit(limit).toList();
        }

        @Override
        public void markPublished(PendingEvent event) {
            marked.add(event);
        }
    }
}
