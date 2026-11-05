package edu.demo.addtask.task.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A unit of work that can be assigned to one person. Immutable: changes return a new task.
 *
 * @param id          identity
 * @param title       never blank
 * @param description free text, empty when absent
 * @param assignment  who the task is assigned to
 * @param createdAt   when the task was added
 * @since 0.1.0
 */
public record Task(UUID id, String title, String description, Assignment assignment, Instant createdAt) {

    public Task {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(assignment, "assignment");
        Objects.requireNonNull(createdAt, "createdAt");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
    }

    /**
     * Creates a task from a draft, assigning it straight away when the draft names an assignee.
     *
     * @param id    identity of the new task
     * @param draft what was asked for
     * @param now   creation time, also used as the assignment time
     * @return the new task
     */
    public static Task create(UUID id, TaskDraft draft, Instant now) {
        Assignment assignment = draft.requestedAssignee()
                .<Assignment>map(assignee -> new Assignment.Assigned(assignee, now))
                .orElse(Assignment.NONE);
        return new Task(id, draft.title().strip(), draft.description().strip(), assignment, now);
    }

    /**
     * @return the assignee's name, empty when unassigned
     */
    public Optional<String> assignee() {
        return switch (assignment) {
            case Assignment.Assigned assigned -> Optional.of(assigned.assignee());
            case Assignment.Unassigned ignored -> Optional.empty();
        };
    }

    /**
     * @return when the current assignment was made, empty when unassigned
     */
    public Optional<Instant> assignedAt() {
        return switch (assignment) {
            case Assignment.Assigned assigned -> Optional.of(assigned.assignedAt());
            case Assignment.Unassigned ignored -> Optional.empty();
        };
    }

    /**
     * @param assignee a person's name
     * @return whether the task is currently assigned to exactly that person
     */
    public boolean isAssignedTo(String assignee) {
        return assignee().filter(assignee::equals).isPresent();
    }

    /**
     * @param assignee the new assignee, never blank
     * @param at       when the assignment happens
     * @return a copy of this task assigned to {@code assignee}
     */
    public Task assignTo(String assignee, Instant at) {
        return new Task(id, title, description, new Assignment.Assigned(assignee, at), createdAt);
    }
}
