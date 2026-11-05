package edu.demo.notificationservice.notification.domain;

/**
 * Restricts a notification search by recipient.
 *
 * @since 0.0.1
 */
public sealed interface RecipientFilter permits RecipientFilter.AnyRecipient, RecipientFilter.VisibleTo {

    /**
     * @param audience a notification's audience
     * @return {@code true} if a notification with that audience passes the filter
     */
    boolean matches(Audience audience);

    /**
     * No restriction.
     */
    record AnyRecipient() implements RecipientFilter {

        @Override
        public boolean matches(Audience audience) {
            return true;
        }
    }

    /**
     * Notifications the named person can see: those addressed to them plus broadcasts.
     *
     * @param name the recipient's name
     */
    record VisibleTo(String name) implements RecipientFilter {

        @Override
        public boolean matches(Audience audience) {
            return switch (audience) {
                case Audience.Everyone _ -> true;
                case Audience.Person person -> person.name().equals(name);
            };
        }
    }
}
