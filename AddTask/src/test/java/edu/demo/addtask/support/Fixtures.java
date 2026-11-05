package edu.demo.addtask.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Reads test documents from {@code src/test/resources/fixtures}.
 */
public final class Fixtures {

    private static final String ROOT = "/fixtures/";

    private Fixtures() {
    }

    /**
     * @param path path below {@code fixtures/}, for example {@code tasks/create-valid.json}
     * @return the file's content as UTF-8 text
     */
    public static String read(String path) {
        try (InputStream stream = Fixtures.class.getResourceAsStream(ROOT + path)) {
            return new String(Objects.requireNonNull(stream, () -> "Missing fixture " + path).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot read fixture " + path, failure);
        }
    }
}
