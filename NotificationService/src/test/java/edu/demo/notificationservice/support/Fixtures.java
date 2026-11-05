package edu.demo.notificationservice.support;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Reads test documents from {@code src/test/resources/fixtures}.
 */
public final class Fixtures {

    private static final String ROOT = "fixtures/";

    private Fixtures() {
    }

    public static String read(String path) {
        try {
            return new ClassPathResource(ROOT + path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read fixture " + path, e);
        }
    }
}
