package edu.demo.notificationservice.notification.text;

import edu.demo.notificationservice.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;

import static edu.demo.notificationservice.support.TaskActivityFixtures.ASSIGNEE;
import static edu.demo.notificationservice.support.TaskActivityFixtures.TITLE;
import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceCatalogTest {

    private static final String BASENAME = "messages";
    private static final List<String> ARGUMENTS = List.of(TITLE, ASSIGNEE);

    private final MessageSourceCatalog catalog = new MessageSourceCatalog(messageSource());

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void render_givenAnyType_namesTheTask(NotificationType type) {
        assertThat(catalog.render(type, ARGUMENTS)).contains(TITLE);
    }

    @Test
    void render_givenTaskUnassigned_namesNewAssignee() {
        assertThat(catalog.render(NotificationType.TASK_UNASSIGNED, ARGUMENTS)).contains(ASSIGNEE);
    }

    private static ResourceBundleMessageSource messageSource() {
        var source = new ResourceBundleMessageSource();
        source.setBasename(BASENAME);
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
