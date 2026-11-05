package edu.demo.notificationservice.notification.application;

import edu.demo.notificationservice.notification.domain.TaskActivity;

/**
 * Inbound port: turns task activity into stored and delivered notifications.
 *
 * @since 0.0.1
 */
public interface TaskActivityRecorder {

    /**
     * Generates, stores and delivers the notifications for an activity. Idempotent per activity event id:
     * recording the same activity again stores and delivers nothing new.
     *
     * @param activity what happened to the task
     */
    void record(TaskActivity activity);
}
