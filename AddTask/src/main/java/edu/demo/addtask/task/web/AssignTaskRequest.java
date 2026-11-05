package edu.demo.addtask.task.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code PUT /api/tasks/{id}/assignee}.
 *
 * @param assignee required, at most 100 characters
 */
record AssignTaskRequest(@NotBlank @Size(max = 100) String assignee) {
}
