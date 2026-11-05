package edu.demo.addtask.task.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Who a task is assigned to, if anyone.
 *
 * @since 0.1.0
 */
public sealed interface Assignment permits Assignment.Unassigned, Assignment.Assigned {

    /** The single unassigned state. */
    Assignment NONE = new Unassigned();

    /** The task has no assignee. */
    record Unassigned() implements Assignment {
    }

    /**
     * The task is assigned to one person.
     *
     * @param assignee   the person's name, never blank
     * @param assignedAt when the assignment was made
     */
    record Assigned(String assignee, Instant assignedAt) implements Assignment {

        public Assigned {
            Objects.requireNonNull(assignedAt, "assignedAt");
            if (assignee == null || assignee.isBlank()) {
                throw new IllegalArgumentException("assignee must not be blank");
            }
        }
    }
}
