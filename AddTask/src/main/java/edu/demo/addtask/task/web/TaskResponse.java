package edu.demo.addtask.task.web;

import edu.demo.addtask.task.domain.Task;

import java.time.Instant;
import java.util.UUID;

/**
 * A task as returned by the API. Absent values are {@code null} in JSON.
 *
 * @param id          task id
 * @param title       task title
 * @param description description, {@code null} when absent
 * @param assignee    current assignee, {@code null} when unassigned
 * @param createdAt   when the task was added
 * @param assignedAt  when the current assignment was made, {@code null} when unassigned
 */
record TaskResponse(UUID id, String title, String description, String assignee, Instant createdAt,
                    Instant assignedAt) {

    static TaskResponse from(Task task) {
        return new TaskResponse(task.id(), task.title(),
                task.description().isEmpty() ? null : task.description(),
                task.assignee().orElse(null), task.createdAt(), task.assignedAt().orElse(null));
    }
}
