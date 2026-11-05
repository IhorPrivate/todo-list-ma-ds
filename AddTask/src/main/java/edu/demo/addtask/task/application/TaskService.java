package edu.demo.addtask.task.application;

import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskDraft;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.domain.TaskNotFoundException;
import edu.demo.addtask.task.event.EventStamp;
import edu.demo.addtask.task.event.TaskAdded;
import edu.demo.addtask.task.event.TaskAssigned;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adds, assigns and reads tasks. Thread-safe: holds no mutable state.
 */
@Service
class TaskService implements TaskCommands, TaskQueries {

    private final TaskStore store;
    private final TaskIds ids;
    private final Clock clock;

    TaskService(TaskStore store, TaskIds ids, Clock clock) {
        this.store = store;
        this.ids = ids;
        this.clock = clock;
    }

    @Override
    public Task add(TaskDraft draft) {
        return addOnce(ids.next(), draft);
    }

    @Override
    public Task addOnce(UUID taskId, TaskDraft draft) {
        var stamp = stamp();
        var task = Task.create(taskId, draft, stamp.occurredAt());
        if (store.insert(task, TaskAdded.of(task, stamp))) {
            return task;
        }
        return store.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    @Override
    public Task assign(TaskAssignment assignment) {
        var task = store.findById(assignment.taskId())
                .orElseThrow(() -> new TaskNotFoundException(assignment.taskId()));
        if (task.isAssignedTo(assignment.assignee())) {
            return task;
        }
        var stamp = stamp();
        var assigned = task.assignTo(assignment.assignee(), stamp.occurredAt());
        store.update(assigned, TaskAssigned.of(task, assigned, stamp));
        return assigned;
    }

    @Override
    public Optional<Task> byId(UUID id) {
        return store.findById(id);
    }

    @Override
    public List<Task> find(TaskFilter filter) {
        return store.find(filter);
    }

    // MongoDB keeps millisecond precision; truncating here keeps returned and re-read tasks equal.
    private EventStamp stamp() {
        return new EventStamp(ids.next(), clock.instant().truncatedTo(ChronoUnit.MILLIS));
    }
}
