package edu.demo.addtask.task.application;

import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskDraft;

import java.util.UUID;

/**
 * Use cases that change tasks. Every change is stored together with the event that announces it.
 *
 * @since 0.1.0
 */
public interface TaskCommands {

    /**
     * Adds a task with a newly generated id.
     *
     * @param draft what to add
     * @return the stored task
     */
    Task add(TaskDraft draft);

    /**
     * Adds a task under a caller-chosen id, at most once: repeating the call with the same id
     * returns the task stored the first time and publishes nothing new.
     *
     * @param taskId id for the task, typically the id of the request that asked for it
     * @param draft  what to add
     * @return the stored task
     */
    Task addOnce(UUID taskId, TaskDraft draft);

    /**
     * Assigns a task. Assigning to the current assignee changes nothing and publishes nothing.
     *
     * @param assignment which task and to whom
     * @return the task after the assignment
     * @throws edu.demo.addtask.task.domain.TaskNotFoundException when the task does not exist
     */
    Task assign(TaskAssignment assignment);
}
