package edu.demo.addtask.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Properties;

/**
 * Wording that tests assert about rules, kept in {@code test-messages.properties} so every test of
 * the same rule states it the same way.
 */
public final class TestMessages {

    private static final String FILE = "/test-messages.properties";
    private static final Properties MESSAGES = load();

    private TestMessages() {
    }

    /**
     * @param key       message key
     * @param arguments values for {@link MessageFormat} placeholders
     * @return the formatted message
     */
    public static String get(String key, Object... arguments) {
        var pattern = Objects.requireNonNull(MESSAGES.getProperty(key), () -> "Missing test message " + key);
        return MessageFormat.format(pattern, arguments);
    }

    private static Properties load() {
        try (InputStream stream = TestMessages.class.getResourceAsStream(FILE)) {
            var properties = new Properties();
            properties.load(Objects.requireNonNull(stream, FILE));
            return properties;
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot read " + FILE, failure);
        }
    }
}
