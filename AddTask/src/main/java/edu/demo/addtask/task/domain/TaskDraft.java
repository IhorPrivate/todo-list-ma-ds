package edu.demo.addtask.task.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * What a caller asks for when adding a task.
 *
 * @param title       the task title, never blank
 * @param description free text, empty when absent
 * @param assignee    the person to assign on creation, empty or blank when nobody
 * @since 0.1.0
 */
public record TaskDraft(String title, String description, String assignee) {

    public TaskDraft {
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(assignee, "assignee");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }

    /**
     * @return the assignee to set on creation, empty when the draft names nobody
     */
    public Optional<String> requestedAssignee() {
        return assignee.isBlank() ? Optional.empty() : Optional.of(assignee.strip());
    }
}
