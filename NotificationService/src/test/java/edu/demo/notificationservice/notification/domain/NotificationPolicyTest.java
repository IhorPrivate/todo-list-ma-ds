package edu.demo.notificationservice.notification.domain;

import org.junit.jupiter.api.Test;

import static edu.demo.notificationservice.support.TaskActivityFixtures.ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.PREVIOUS_ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.TITLE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.addedFor;
import static edu.demo.notificationservice.support.TaskActivityFixtures.assigned;
import static edu.demo.notificationservice.support.TaskActivityFixtures.reassignedFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class NotificationPolicyTest {

    private final NotificationPolicy policy = new NotificationPolicy();

    @Test
    void plan_givenTaskAddedForAssignee_notifiesAssignee() {
        var plans = policy.plan(addedFor(new Audience.Person(ASSIGNEE)));

        assertThat(plans)
                .extracting(PlannedNotification::type, PlannedNotification::audience)
                .containsExactly(tuple(NotificationType.TASK_ADDED, new Audience.Person(ASSIGNEE)));
    }

    @Test
    void plan_givenUnassignedTaskAdded_broadcasts() {
        var plans = policy.plan(addedFor(new Audience.Everyone()));

        assertThat(plans)
                .extracting(PlannedNotification::type, PlannedNotification::audience)
                .containsExactly(tuple(NotificationType.TASK_ADDED, new Audience.Everyone()));
    }

    @Test
    void plan_givenTaskAssigned_notifiesNewAssignee() {
        var plans = policy.plan(assigned());

        assertThat(plans)
                .extracting(PlannedNotification::type, PlannedNotification::audience)
                .containsExactly(tuple(NotificationType.TASK_ASSIGNED, new Audience.Person(ASSIGNEE)));
    }

    @Test
    void plan_givenTaskReassigned_notifiesNewAndPreviousAssignee() {
        var plans = policy.plan(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(plans)
                .extracting(PlannedNotification::type, PlannedNotification::audience)
                .containsExactly(
                        tuple(NotificationType.TASK_ASSIGNED, new Audience.Person(ASSIGNEE)),
                        tuple(NotificationType.TASK_UNASSIGNED, new Audience.Person(PREVIOUS_ASSIGNEE)));
    }

    @Test
    void plan_givenReassignedToSameAssignee_notifiesOnlyAssignee() {
        var plans = policy.plan(reassignedFrom(ASSIGNEE));

        assertThat(plans)
                .extracting(PlannedNotification::type)
                .containsExactly(NotificationType.TASK_ASSIGNED);
    }

    @Test
    void plan_givenTaskReassigned_tellsPreviousAssigneeWhoTookOver() {
        var plans = policy.plan(reassignedFrom(PREVIOUS_ASSIGNEE));

        assertThat(plans.getLast().messageArguments()).containsExactly(TITLE, ASSIGNEE);
    }
}
