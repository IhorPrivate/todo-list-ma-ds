package edu.demo.addtask.task.messaging;

/**
 * Sends one stored event to the broker and returns only once the broker has accepted it.
 */
interface EventPublisher {

    /**
     * @param event the event to publish
     * @throws EventPublishException when the broker did not confirm or could not route the event
     */
    void publish(PendingEvent event);
}
