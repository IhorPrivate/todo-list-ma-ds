package edu.demo.notificationservice.notification.domain;

/**
 * Who a notification is addressed to: one named person, or everyone.
 *
 * @since 0.0.1
 */
public sealed interface Audience permits Audience.Everyone, Audience.Person {

    /**
     * A stable text key identifying this audience, used to derive deterministic notification ids.
     *
     * @return the audience key; never empty
     */
    String key();

    /**
     * A broadcast audience: the notification is visible to every recipient.
     */
    record Everyone() implements Audience {

        private static final String KEY = "everyone";

        @Override
        public String key() {
            return KEY;
        }
    }

    /**
     * A single named recipient.
     *
     * @param name the recipient's name, as used by the task service for assignees
     */
    record Person(String name) implements Audience {

        private static final String KEY_PREFIX = "person:";

        @Override
        public String key() {
            return KEY_PREFIX + name;
        }
    }
}
