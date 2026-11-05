package edu.demo.notificationservice.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Sentences tests assert about rules, from {@code src/test/resources/test-messages.properties}.
 */
public final class TestMessages {

    private static final String RESOURCE = "/test-messages.properties";
    private static final Properties MESSAGES = load();

    private TestMessages() {
    }

    public static String get(String key) {
        var message = MESSAGES.getProperty(key);
        if (message == null) {
            throw new IllegalArgumentException("No test message for key " + key);
        }
        return message;
    }

    private static Properties load() {
        try (InputStream stream = TestMessages.class.getResourceAsStream(RESOURCE)) {
            var properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + RESOURCE, e);
        }
    }
}
