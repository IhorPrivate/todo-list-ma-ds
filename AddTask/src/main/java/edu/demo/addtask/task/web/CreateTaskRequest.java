package edu.demo.addtask.task.web;

import edu.demo.addtask.task.domain.TaskDraft;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /api/tasks}.
 *
 * @param title       required, at most 200 characters
 * @param description optional, at most 2000 characters
 * @param assignee    optional assignee on creation, at most 100 characters
 */
record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 100) String assignee) {

    TaskDraft toDraft() {
        return new TaskDraft(title, description == null ? "" : description, assignee == null ? "" : assignee);
    }
}
