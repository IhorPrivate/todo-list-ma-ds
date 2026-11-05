package edu.demo.addtask.task.messaging;

/**
 * Names of every exchange, queue and routing key this service uses, shared by declarations,
 * listeners, publishers and tests.
 */
public final class TasksTopology {

    /** Direct exchange other services send task commands to. */
    public static final String COMMANDS_EXCHANGE = "tasks.commands";
    /** Topic exchange this service publishes task events on. */
    public static final String EVENTS_EXCHANGE = "tasks.events";
    /** Dead-letter exchange for this service's queues. */
    public static final String DEAD_LETTER_EXCHANGE = "addtask.dead-letters";

    public static final String ADD_TASK_KEY = "task.add";
    public static final String ASSIGN_TASK_KEY = "task.assign";
    public static final String TASK_ADDED_KEY = "task.added";
    public static final String TASK_ASSIGNED_KEY = "task.assigned";

    public static final String ADD_TASK_QUEUE = "addtask.add-task";
    public static final String ASSIGN_TASK_QUEUE = "addtask.assign-task";
    public static final String ADD_TASK_DLQ = ADD_TASK_QUEUE + ".dlq";
    public static final String ASSIGN_TASK_DLQ = ASSIGN_TASK_QUEUE + ".dlq";

    public static final String ADD_TASK_LISTENER = "add-task-listener";
    public static final String ASSIGN_TASK_LISTENER = "assign-task-listener";

    private TasksTopology() {
    }
}
