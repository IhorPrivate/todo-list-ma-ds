package edu.demo.notificationservice.notification.web;

import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.notification.domain.ReadFilter;
import edu.demo.notificationservice.notification.domain.RecipientFilter;
import jakarta.validation.constraints.Size;

/**
 * Query parameters of {@code GET /api/notifications}. Both are optional.
 *
 * @param recipient only notifications this person can see (theirs plus broadcasts)
 * @param unread    {@code true} for unread only, {@code false} for read only, absent for both
 */
record NotificationSearch(@Size(max = 100) String recipient, Boolean unread) {

    NotificationFilter toFilter() {
        return new NotificationFilter(recipientFilter(), readFilter());
    }

    private RecipientFilter recipientFilter() {
        return recipient == null || recipient.isBlank()
                ? new RecipientFilter.AnyRecipient()
                : new RecipientFilter.VisibleTo(recipient);
    }

    private ReadFilter readFilter() {
        if (unread == null) {
            return ReadFilter.ANY;
        }
        return unread ? ReadFilter.UNREAD : ReadFilter.READ;
    }
}
