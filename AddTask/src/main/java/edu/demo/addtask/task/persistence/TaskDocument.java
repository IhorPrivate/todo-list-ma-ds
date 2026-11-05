package edu.demo.addtask.task.persistence;

import edu.demo.addtask.task.domain.Assignment;
import edu.demo.addtask.task.domain.Task;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Storage shape of a task. Unpublished events live in {@code outbox} inside the same document, so a
 * task change and its event are written in one atomic single-document operation without needing a
 * replica set for transactions.
 */
@Document(TaskDocument.COLLECTION)
record TaskDocument(
        @Id String id,
        String title,
        String description,
        String assignee,
        Instant assignedAt,
        Instant createdAt,
        List<OutboxEntryDocument> outbox) {

    static final String COLLECTION = "tasks";
    static final String ID = "_id";
    static final String TITLE = "title";
    static final String DESCRIPTION = "description";
    static final String ASSIGNEE = "assignee";
    static final String ASSIGNED_AT = "assignedAt";
    static final String CREATED_AT = "createdAt";
    static final String OUTBOX = "outbox";
    static final String OUTBOX_EVENT_ID = "eventId";
    static final String FIRST_OUTBOX_ENTRY = OUTBOX + ".0";

    static TaskDocument from(Task task, OutboxEntryDocument pending) {
        return new TaskDocument(task.id().toString(), task.title(), task.description(),
                task.assignee().orElse(null), task.assignedAt().orElse(null), task.createdAt(), List.of(pending));
    }

    Task toTask() {
        Assignment assignment = assignee == null ? Assignment.NONE : new Assignment.Assigned(assignee, assignedAt);
        return new Task(UUID.fromString(id), title, description, assignment, createdAt);
    }
}
