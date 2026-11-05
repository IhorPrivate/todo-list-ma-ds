package edu.demo.addtask.task.application;

import java.util.UUID;

/**
 * Source of new identities for tasks and events, behind an interface so tests stay deterministic.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface TaskIds {

    /**
     * @return a new, unique id
     */
    UUID next();
}
