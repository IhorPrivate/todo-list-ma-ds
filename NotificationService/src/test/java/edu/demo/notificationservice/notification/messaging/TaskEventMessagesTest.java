package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.TaskActivity;
import edu.demo.notificationservice.support.Fixtures;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskEventMessagesTest {

    private static final String TASK_ADDED = "events/task-added.json";
    private static final String TASK_ADDED_UNASSIGNED = "events/task-added-unassigned.json";
    private static final String TASK_ADDED_MISSING_TASK_ID = "events/task-added-missing-task-id.json";
    private static final String TASK_ASSIGNED = "events/task-assigned.json";
    private static final String TASK_REASSIGNED = "events/task-reassigned.json";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void toActivity_givenTaskAddedWithAssignee_addressesAssignee() {
        var message = read(TASK_ADDED, TaskAddedMessage.class);

        assertThat(message.toActivity().audience()).isEqualTo(new Audience.Person(message.assignee()));
    }

    @Test
    void toActivity_givenUnassignedTaskAdded_addressesEveryone() {
        var message = read(TASK_ADDED_UNASSIGNED, TaskAddedMessage.class);

        assertThat(message.toActivity().audience()).isEqualTo(new Audience.Everyone());
    }

    @Test
    void toActivity_givenTaskAdded_carriesEventAndTaskIds() {
        var message = read(TASK_ADDED, TaskAddedMessage.class);

        assertThat(message.toActivity())
                .returns(message.eventId(), TaskActivity::eventId)
                .returns(message.taskId(), TaskActivity::taskId);
    }

    @Test
    void toActivity_givenTaskAddedWithoutTaskId_rejectsAsMalformed() {
        var message = read(TASK_ADDED_MISSING_TASK_ID, TaskAddedMessage.class);

        assertThatThrownBy(message::toActivity).isInstanceOf(MalformedEventException.class);
    }

    @Test
    void toActivity_givenFirstAssignment_mapsToAssigned() {
        var message = read(TASK_ASSIGNED, TaskAssignedMessage.class);

        assertThat(message.toActivity()).isInstanceOf(TaskActivity.Assigned.class);
    }

    @Test
    void toActivity_givenAssignmentWithPreviousAssignee_mapsToReassigned() {
        var message = read(TASK_REASSIGNED, TaskAssignedMessage.class);

        assertThat(message.toActivity())
                .isInstanceOfSatisfying(TaskActivity.Reassigned.class, reassigned ->
                        assertThat(reassigned.previousAssignee()).isEqualTo(message.previousAssignee()));
    }

    private <T> T read(String fixture, Class<T> type) {
        return jsonMapper.readValue(Fixtures.read(fixture), type);
    }
}
