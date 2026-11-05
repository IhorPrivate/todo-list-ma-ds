package edu.demo.addtask.task.event;

import java.util.UUID;

/**
 * Published facts about tasks. The records are the wire contract on the {@code tasks.events}
 * exchange: add fields freely, never rename or change their meaning.
 *
 * @since 0.1.0
 */
public sealed interface TaskEvent permits TaskAdded, TaskAssigned {

    /**
     * @return unique id of this event, also sent as the AMQP message id
     */
    UUID eventId();

    /**
     * @return the task the event is about
     */
    UUID taskId();
}
