package edu.demo.notificationservice.notification.text;

import edu.demo.notificationservice.notification.application.MessageCatalog;
import edu.demo.notificationservice.notification.domain.NotificationType;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Renders notification text from {@code messages.properties}, one key per notification type.
 */
@Component
class MessageSourceCatalog implements MessageCatalog {

    private static final String KEY_PREFIX = "notification.";

    private final MessageSource messageSource;

    MessageSourceCatalog(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String render(NotificationType type, List<String> arguments) {
        return messageSource.getMessage(keyFor(type), arguments.toArray(), Locale.ROOT);
    }

    static String keyFor(NotificationType type) {
        return KEY_PREFIX + type.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
