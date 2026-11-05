package edu.demo.addtask.task.application;

import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only access to tasks.
 *
 * @since 0.1.0
 */
public interface TaskQueries {

    /**
     * @param id task id
     * @return the task, empty when it does not exist
     */
    Optional<Task> byId(UUID id);

    /**
     * @param filter which tasks to include
     * @return matching tasks, newest first; empty when none match
     */
    List<Task> find(TaskFilter filter);
}
