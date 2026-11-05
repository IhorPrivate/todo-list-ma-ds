package edu.demo.addtask.task.domain;

import java.util.UUID;

/**
 * Thrown when an operation names a task that does not exist.
 *
 * @since 0.1.0
 */
public class TaskNotFoundException extends RuntimeException {

    private final UUID taskId;

    /**
     * @param taskId the id that was not found
     */
    public TaskNotFoundException(UUID taskId) {
        super("Task " + taskId + " was not found");
        this.taskId = taskId;
    }

    /**
     * @return the id that was not found
     */
    public UUID taskId() {
        return taskId;
    }
}
