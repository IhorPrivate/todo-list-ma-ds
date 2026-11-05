package edu.demo.addtask.task.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TaskTest {

    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final Instant CREATED = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant LATER = Instant.parse("2026-11-03T11:00:00Z");
    private static final String TITLE = "Write release notes";
    private static final String PADDED_TITLE = "  " + TITLE + "  ";
    private static final String NO_TEXT = "";
    private static final String ALICE = "alice";
    private static final String BOB = "bob";

    @Test
    void create_givenDraftWithoutAssignee_isUnassigned() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, NO_TEXT), CREATED);

        assertThat(task.assignment()).isEqualTo(Assignment.NONE);
    }

    @Test
    void create_givenDraftWithAssignee_isAssignedAtCreation() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, ALICE), CREATED);

        assertThat(task.assignment()).isEqualTo(new Assignment.Assigned(ALICE, CREATED));
    }

    @Test
    void create_givenPaddedTitle_stripsIt() {
        var task = Task.create(TASK_ID, new TaskDraft(PADDED_TITLE, NO_TEXT, NO_TEXT), CREATED);

        assertThat(task.title()).isEqualTo(TITLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void draft_givenBlankTitle_isRejected(String blankTitle) {
        assertThatIllegalArgumentException().isThrownBy(() -> new TaskDraft(blankTitle, NO_TEXT, NO_TEXT));
    }

    @Test
    void assignTo_givenNewAssignee_replacesAssignment() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, ALICE), CREATED);

        var reassigned = task.assignTo(BOB, LATER);

        assertThat(reassigned.assignment()).isEqualTo(new Assignment.Assigned(BOB, LATER));
    }

    @Test
    void isAssignedTo_givenCurrentAssignee_isTrue() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, ALICE), CREATED);

        assertThat(task.isAssignedTo(ALICE)).isTrue();
    }

    @Test
    void isAssignedTo_givenUnassignedTask_isFalse() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, NO_TEXT), CREATED);

        assertThat(task.isAssignedTo(ALICE)).isFalse();
    }

    @Test
    void assignee_givenUnassignedTask_isEmpty() {
        var task = Task.create(TASK_ID, new TaskDraft(TITLE, NO_TEXT, NO_TEXT), CREATED);

        assertThat(task.assignee()).isEmpty();
    }
}
