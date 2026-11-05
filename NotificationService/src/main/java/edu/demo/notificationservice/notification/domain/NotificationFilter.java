package edu.demo.notificationservice.notification.domain;

/**
 * Search criteria for notifications. The single definition of filter semantics; storage adapters translate it.
 *
 * @param recipient restriction by recipient
 * @param read      restriction by read state
 * @since 0.0.1
 */
public record NotificationFilter(RecipientFilter recipient, ReadFilter read) {

    /**
     * @return a filter that matches every notification
     */
    public static NotificationFilter all() {
        return new NotificationFilter(new RecipientFilter.AnyRecipient(), ReadFilter.ANY);
    }

    /**
     * @param notification a notification
     * @return {@code true} if it satisfies both restrictions
     */
    public boolean matches(Notification notification) {
        return recipient.matches(notification.audience()) && read.matches(notification.readStatus());
    }
}
