package edu.demo.notificationservice.support;

import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.TaskActivity;

import java.util.UUID;

/**
 * Task activities shared by domain, service and adapter tests.
 */
public final class TaskActivityFixtures {

    public static final UUID EVENT_ID = UUID.fromString("0e8b1c3d-5f7a-4b2c-8d9e-1a2b3c4d5e01");
    public static final UUID OTHER_EVENT_ID = UUID.fromString("0e8b1c3d-5f7a-4b2c-8d9e-1a2b3c4d5e02");
    public static final UUID TASK_ID = UUID.fromString("7c6d5e4f-3a2b-4c1d-9e8f-0a1b2c3d4e01");
    public static final String TITLE = "Write onboarding guide";
    public static final String ASSIGNEE = "dana";
    public static final String PREVIOUS_ASSIGNEE = "erik";

    private TaskActivityFixtures() {
    }

    public static TaskActivity.Added addedFor(Audience audience) {
        return new TaskActivity.Added(EVENT_ID, TASK_ID, TITLE, audience);
    }

    public static TaskActivity.Assigned assigned() {
        return new TaskActivity.Assigned(EVENT_ID, TASK_ID, TITLE, ASSIGNEE);
    }

    public static TaskActivity.Reassigned reassignedFrom(String previousAssignee) {
        return new TaskActivity.Reassigned(EVENT_ID, TASK_ID, TITLE, ASSIGNEE, previousAssignee);
    }
}
