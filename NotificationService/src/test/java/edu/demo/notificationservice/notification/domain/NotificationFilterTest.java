package edu.demo.notificationservice.notification.domain;

import edu.demo.notificationservice.support.NotificationFixtures;
import edu.demo.notificationservice.support.TestMessages;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationFilterTest {

    private static final String VIEWER = "frank";
    private static final String SOMEONE_ELSE = "grace";

    static Stream<Arguments> audiencesAndVisibility() {
        return Stream.of(
                Arguments.of(new Audience.Person(VIEWER), true),
                Arguments.of(new Audience.Everyone(), true),
                Arguments.of(new Audience.Person(SOMEONE_ELSE), false));
    }

    @ParameterizedTest
    @MethodSource("audiencesAndVisibility")
    void matches_givenVisibleToFilter_includesOwnAndBroadcastNotifications(Audience audience, boolean visible) {
        var filter = new NotificationFilter(new RecipientFilter.VisibleTo(VIEWER), ReadFilter.ANY);

        assertThat(filter.matches(NotificationFixtures.unread(audience)))
                .as(TestMessages.get("filter.visible-includes-broadcasts"))
                .isEqualTo(visible);
    }

    @ParameterizedTest
    @EnumSource(ReadFilter.class)
    void matches_givenReadFilter_selectsByReadState(ReadFilter readFilter) {
        var filter = new NotificationFilter(new RecipientFilter.AnyRecipient(), readFilter);
        var read = NotificationFixtures.read(new Audience.Everyone());
        var unread = NotificationFixtures.unread(new Audience.Everyone());

        assertThat(new boolean[] {filter.matches(unread), filter.matches(read)})
                .containsExactly(readFilter != ReadFilter.READ, readFilter != ReadFilter.UNREAD);
    }
}
