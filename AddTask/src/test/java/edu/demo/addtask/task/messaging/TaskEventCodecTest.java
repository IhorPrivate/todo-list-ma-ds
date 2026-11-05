package edu.demo.addtask.task.messaging;

import edu.demo.addtask.support.Fixtures;
import edu.demo.addtask.task.event.TaskAdded;
import edu.demo.addtask.task.event.TaskAssigned;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract tests: the JSON on {@code tasks.events} is what NotificationService consumes.
 */
class TaskEventCodecTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID ADDED_EVENT = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
    private static final UUID ASSIGNED_EVENT = UUID.fromString("00000000-0000-0000-0000-0000000000e2");
    private static final Instant OCCURRED = Instant.parse("2026-11-03T10:15:30Z");
    private static final String TITLE = "Write release notes";
    private static final String ALICE = "alice";
    private static final String BOB = "bob";
    private static final TaskAdded UNASSIGNED_TASK_ADDED = new TaskAdded(ADDED_EVENT, OCCURRED, TASK_ID, TITLE, null, null);
    private static final TaskAssigned REASSIGNED = new TaskAssigned(ASSIGNED_EVENT, OCCURRED, TASK_ID, TITLE, BOB, ALICE);

    private final TaskEventCodec codec = new TaskEventCodec(JSON);

    @Test
    void toJson_givenTaskAdded_matchesPublishedContract() {
        assertThat(JSON.readTree(codec.toJson(UNASSIGNED_TASK_ADDED)))
                .isEqualTo(JSON.readTree(Fixtures.read("events/task-added.json")));
    }

    @Test
    void toJson_givenTaskAssigned_matchesPublishedContract() {
        assertThat(JSON.readTree(codec.toJson(REASSIGNED)))
                .isEqualTo(JSON.readTree(Fixtures.read("events/task-assigned.json")));
    }

    @Test
    void routingKey_givenEachEventType_usesItsKey() {
        assertThat(codec.routingKey(UNASSIGNED_TASK_ADDED)).isEqualTo(TasksTopology.TASK_ADDED_KEY);
        assertThat(codec.routingKey(REASSIGNED)).isEqualTo(TasksTopology.TASK_ASSIGNED_KEY);
    }
}
