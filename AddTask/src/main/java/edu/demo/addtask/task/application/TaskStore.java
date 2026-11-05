package edu.demo.addtask.task.application;

import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.event.TaskEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for tasks. Each write stores the task and the event announcing the change
 * atomically, so an event is published if and only if its change was stored.
 *
 * @since 0.1.0
 */
public interface TaskStore {

    /**
     * @param task  a task that may not exist yet
     * @param event the event announcing its addition
     * @return {@code true} when stored, {@code false} when a task with that id already exists
     */
    boolean insert(Task task, TaskEvent event);

    /**
     * @param task  the new state of an existing task
     * @param event the event announcing the change
     */
    void update(Task task, TaskEvent event);

    /**
     * @param id task id
     * @return the task, empty when absent
     */
    Optional<Task> findById(UUID id);

    /**
     * @param filter which tasks to include
     * @return matching tasks, newest first
     */
    List<Task> find(TaskFilter filter);
}
