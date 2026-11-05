package edu.demo.notificationservice.notification.messaging;

import edu.demo.notificationservice.notification.application.TaskActivityRecorder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Receives task events and hands them to the recording port. Exceptions propagate so the container retries
 * transient failures and dead-letters poison messages.
 */
@Component
class TaskEventsListener {

    private final TaskActivityRecorder recorder;

    TaskEventsListener(TaskActivityRecorder recorder) {
        this.recorder = recorder;
    }

    @RabbitListener(id = TaskEventsTopology.TASK_ADDED_LISTENER, queues = TaskEventsTopology.TASK_ADDED_QUEUE)
    void onTaskAdded(TaskAddedMessage message) {
        recorder.record(message.toActivity());
    }

    @RabbitListener(id = TaskEventsTopology.TASK_ASSIGNED_LISTENER, queues = TaskEventsTopology.TASK_ASSIGNED_QUEUE)
    void onTaskAssigned(TaskAssignedMessage message) {
        recorder.record(message.toActivity());
    }
}
