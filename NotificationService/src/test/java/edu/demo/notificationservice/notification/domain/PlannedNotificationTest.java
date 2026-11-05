package edu.demo.notificationservice.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static edu.demo.notificationservice.support.TaskActivityFixtures.ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.OTHER_EVENT_ID;
import static edu.demo.notificationservice.support.TaskActivityFixtures.PREVIOUS_ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.TASK_ID;
import static edu.demo.notificationservice.support.TaskActivityFixtures.TITLE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.addedFor;
import static org.assertj.core.api.Assertions.assertThat;

class PlannedNotificationTest {

    private static final Instant CREATED_AT = Instant.parse("2026-11-03T08:00:00.000Z");
    private static final String MESSAGE = "Rendered text";

    @Test
    void id_givenSamePlanTwice_isEqual() {
        assertThat(planFor(new Audience.Person(ASSIGNEE)).id()).isEqualTo(planFor(new Audience.Person(ASSIGNEE)).id());
    }

    @Test
    void id_givenDifferentAudience_differs() {
        assertThat(planFor(new Audience.Person(ASSIGNEE)).id())
                .isNotEqualTo(planFor(new Audience.Person(PREVIOUS_ASSIGNEE)).id());
    }

    @Test
    void id_givenDifferentSourceEvent_differs() {
        var otherEvent = new TaskActivity.Added(OTHER_EVENT_ID, TASK_ID, TITLE, new Audience.Person(ASSIGNEE));
        var fromOtherEvent = new PlannedNotification(
                otherEvent, NotificationType.TASK_ADDED, new Audience.Person(ASSIGNEE), List.of(TITLE));

        assertThat(fromOtherEvent.id()).isNotEqualTo(planFor(new Audience.Person(ASSIGNEE)).id());
    }

    @Test
    void toNotification_givenPlan_createsUnreadNotificationOfTheTask() {
        var notification = planFor(new Audience.Everyone()).toNotification(MESSAGE, CREATED_AT);

        assertThat(notification)
                .returns(TASK_ID, Notification::taskId)
                .returns(CREATED_AT, Notification::createdAt)
                .returns(false, Notification::isRead);
    }

    private static PlannedNotification planFor(Audience audience) {
        return new PlannedNotification(addedFor(audience), NotificationType.TASK_ADDED, audience, List.of(TITLE));
    }
}
