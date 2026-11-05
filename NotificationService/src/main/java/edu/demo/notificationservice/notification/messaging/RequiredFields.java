package edu.demo.notificationservice.notification.messaging;

/**
 * Presence checks for fields of incoming events, the one boundary where {@code null} can enter.
 */
final class RequiredFields {

    private RequiredFields() {
    }

    static <T> T require(T value, String eventType, String field) {
        if (value == null || value instanceof String text && text.isBlank()) {
            throw new MalformedEventException(eventType, field);
        }
        return value;
    }
}
