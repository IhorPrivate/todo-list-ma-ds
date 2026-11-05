package edu.demo.addtask.task.messaging;

import java.util.List;

/**
 * Outbox port: events written atomically with task changes, waiting to be published.
 */
public interface PendingEvents {

    /**
     * @param limit most events to return
     * @return unpublished events, each task's events in the order they were written
     */
    List<PendingEvent> oldest(int limit);

    /**
     * @param event an event the broker confirmed; it will not be returned again
     */
    void markPublished(PendingEvent event);
}
