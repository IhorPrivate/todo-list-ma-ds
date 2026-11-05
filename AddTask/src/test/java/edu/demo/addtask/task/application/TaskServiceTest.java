package edu.demo.addtask.task.application;

import edu.demo.addtask.support.TestMessages;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskDraft;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.domain.TaskNotFoundException;
import edu.demo.addtask.task.event.TaskAdded;
import edu.demo.addtask.task.event.TaskAssigned;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TaskServiceTest {

    // Time and identity
    private static final Instant NOW = Instant.parse("2026-11-03T10:15:30.123456Z");
    private static final Instant NOW_AT_STORED_PRECISION = Instant.parse("2026-11-03T10:15:30.123Z");
    private static final UUID FIRST_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID THIRD_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID FOURTH_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final UUID UNKNOWN_TASK = UUID.fromString("00000000-0000-0000-0000-0000000000ff");

    // Task content
    private static final String TITLE = "Write release notes";
    private static final String OTHER_TITLE = "Review pull request";
    private static final String NO_TEXT = "";
    private static final String ALICE = "alice";
    private static final String BOB = "bob";

    private InMemoryTaskStore store;
    private TaskService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryTaskStore();
        var ids = new ArrayDeque<>(List.of(FIRST_ID, SECOND_ID, THIRD_ID, FOURTH_ID));
        service = new TaskService(store, ids::removeFirst, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void add_givenDraft_storesTaskWithAddedEvent() {
        var task = service.add(new TaskDraft(TITLE, NO_TEXT, ALICE));

        assertThat(store.events()).containsExactly(
                new TaskAdded(SECOND_ID, NOW_AT_STORED_PRECISION, task.id(), TITLE, null, ALICE));
    }

    @Test
    void add_givenDraft_truncatesTimesToStoredPrecision() {
        var task = service.add(new TaskDraft(TITLE, NO_TEXT, NO_TEXT));

        assertThat(task.createdAt()).isEqualTo(NOW_AT_STORED_PRECISION);
    }

    @Test
    void addOnce_givenRepeatedRequestId_returnsFirstTaskWithoutSecondEvent() {
        var first = service.addOnce(REQUEST_ID, new TaskDraft(TITLE, NO_TEXT, NO_TEXT));

        var repeated = service.addOnce(REQUEST_ID, new TaskDraft(OTHER_TITLE, NO_TEXT, NO_TEXT));

        assertThat(repeated).isEqualTo(first);
        assertThat(store.events()).hasSize(1);
    }

    @Test
    void assign_givenNewAssignee_storesAssignedEventWithPreviousAssignee() {
        var task = service.add(new TaskDraft(TITLE, NO_TEXT, ALICE));

        service.assign(new TaskAssignment(task.id(), BOB));

        assertThat(store.events()).last().isEqualTo(
                new TaskAssigned(THIRD_ID, NOW_AT_STORED_PRECISION, task.id(), TITLE, BOB, ALICE));
    }

    @Test
    void assign_givenCurrentAssignee_changesNothing() {
        var task = service.add(new TaskDraft(TITLE, NO_TEXT, ALICE));

        var unchanged = service.assign(new TaskAssignment(task.id(), ALICE));

        assertThat(unchanged).isEqualTo(task);
        assertThat(store.events()).hasSize(1);
    }

    @Test
    void assign_givenUnknownTask_throwsNotFound() {
        assertThatExceptionOfType(TaskNotFoundException.class)
                .isThrownBy(() -> service.assign(new TaskAssignment(UNKNOWN_TASK, BOB)))
                .withMessage(TestMessages.get("task.not-found", UNKNOWN_TASK));
    }

    @Test
    void find_givenAssigneeFilter_returnsOnlyTheirTasks() {
        var alicesTask = service.add(new TaskDraft(TITLE, NO_TEXT, ALICE));
        service.add(new TaskDraft(OTHER_TITLE, NO_TEXT, BOB));

        assertThat(service.find(new TaskFilter(ALICE))).containsExactly(alicesTask);
    }

    @Test
    void byId_givenUnknownTask_isEmpty() {
        assertThat(service.byId(UNKNOWN_TASK)).isEmpty();
    }
}
