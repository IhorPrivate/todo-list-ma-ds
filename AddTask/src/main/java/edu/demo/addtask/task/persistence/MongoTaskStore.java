package edu.demo.addtask.task.persistence;

import edu.demo.addtask.task.application.TaskStore;
import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.event.TaskEvent;
import edu.demo.addtask.task.messaging.PendingEvent;
import edu.demo.addtask.task.messaging.PendingEvents;
import edu.demo.addtask.task.messaging.TaskEventCodec;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB adapter for tasks and their outbox.
 */
@Repository
class MongoTaskStore implements TaskStore, PendingEvents {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, TaskDocument.CREATED_AT);
    private static final Sort OLDEST_FIRST = Sort.by(Sort.Direction.ASC, TaskDocument.CREATED_AT);

    private final MongoOperations mongo;
    private final TaskEventCodec codec;

    MongoTaskStore(MongoOperations mongo, TaskEventCodec codec) {
        this.mongo = mongo;
        this.codec = codec;
    }

    @Override
    public boolean insert(Task task, TaskEvent event) {
        try {
            mongo.insert(TaskDocument.from(task, outboxEntry(event, task.createdAt())));
            return true;
        } catch (DuplicateKeyException alreadyStored) {
            return false;
        }
    }

    @Override
    public void update(Task task, TaskEvent event) {
        var occurredAt = task.assignedAt().orElse(task.createdAt());
        var update = new Update()
                .set(TaskDocument.TITLE, task.title())
                .set(TaskDocument.DESCRIPTION, task.description())
                .set(TaskDocument.ASSIGNEE, task.assignee().orElse(null))
                .set(TaskDocument.ASSIGNED_AT, task.assignedAt().orElse(null))
                .push(TaskDocument.OUTBOX, outboxEntry(event, occurredAt));
        mongo.updateFirst(byId(task.id()), update, TaskDocument.class);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(mongo.findOne(byId(id), TaskDocument.class)).map(TaskDocument::toTask);
    }

    @Override
    public List<Task> find(TaskFilter filter) {
        var query = filter.requiredAssignee()
                .map(assignee -> Query.query(Criteria.where(TaskDocument.ASSIGNEE).is(assignee)))
                .orElseGet(Query::new)
                .with(NEWEST_FIRST);
        return mongo.find(query, TaskDocument.class).stream().map(TaskDocument::toTask).toList();
    }

    @Override
    public List<PendingEvent> oldest(int limit) {
        var query = Query.query(Criteria.where(TaskDocument.FIRST_OUTBOX_ENTRY).exists(true))
                .with(OLDEST_FIRST)
                .limit(limit);
        return mongo.find(query, TaskDocument.class).stream()
                .flatMap(document -> document.outbox().stream().map(entry -> new PendingEvent(
                        UUID.fromString(document.id()), UUID.fromString(entry.eventId()), entry.routingKey(),
                        entry.payload())))
                .limit(limit)
                .toList();
    }

    @Override
    public void markPublished(PendingEvent event) {
        var published = Query.query(Criteria.where(TaskDocument.OUTBOX_EVENT_ID).is(event.eventId().toString()));
        mongo.updateFirst(byId(event.taskId()), new Update().pull(TaskDocument.OUTBOX, published), TaskDocument.class);
    }

    private OutboxEntryDocument outboxEntry(TaskEvent event, Instant occurredAt) {
        return new OutboxEntryDocument(event.eventId().toString(), codec.routingKey(event), codec.toJson(event),
                occurredAt);
    }

    private static Query byId(UUID id) {
        return Query.query(Criteria.where(TaskDocument.ID).is(id.toString()));
    }
}
