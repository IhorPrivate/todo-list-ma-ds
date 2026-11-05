package edu.demo.addtask.task.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * Narrows a task listing.
 *
 * @param assignee only tasks assigned to this person; blank means no restriction
 * @since 0.1.0
 */
public record TaskFilter(String assignee) {

    /** A filter that matches every task. */
    public static final TaskFilter ANY = new TaskFilter("");

    public TaskFilter {
        Objects.requireNonNull(assignee, "assignee");
    }

    /**
     * @return the assignee to filter on, empty when every task matches
     */
    public Optional<String> requiredAssignee() {
        return assignee.isBlank() ? Optional.empty() : Optional.of(assignee.strip());
    }
}
