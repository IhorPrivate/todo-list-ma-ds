package edu.demo.notificationservice.notification.web;

import edu.demo.notificationservice.notification.application.NotificationQueries;
import edu.demo.notificationservice.notification.application.NotificationReadMarker;
import edu.demo.notificationservice.notification.domain.Audience;
import edu.demo.notificationservice.notification.domain.NotificationFilter;
import edu.demo.notificationservice.notification.domain.ReadFilter;
import edu.demo.notificationservice.notification.domain.RecipientFilter;
import edu.demo.notificationservice.support.NotificationFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    private static final String NOTIFICATIONS = "/api/notifications";
    private static final String NOTIFICATION = "/api/notifications/{id}";
    private static final String MARK_READ = "/api/notifications/{id}/read";

    private static final String RECIPIENT_PARAM = "recipient";
    private static final String UNREAD_PARAM = "unread";
    private static final String RECIPIENT = "henry";
    private static final String TOO_LONG_RECIPIENT = "r".repeat(101);
    private static final UUID UNKNOWN_ID = UUID.fromString("00000000-0000-4000-8000-000000000404");
    private static final String MALFORMED_ID = "not-a-uuid";

    private static final String ID_PATH = "$.id";
    private static final String RECIPIENT_PATH = "$.recipient";
    private static final String READ_AT_PATH = "$.readAt";
    private static final String TYPE_PATH = "$.type";
    private static final String FIRST_ID_PATH = "$[0].id";

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private NotificationQueries queries;

    @MockitoBean
    private NotificationReadMarker readMarker;

    @Test
    void search_givenNoParameters_searchesEverything() {
        given(queries.find(any())).willReturn(List.of());

        assertThat(mvc.get().uri(NOTIFICATIONS)).hasStatusOk();

        verify(queries).find(NotificationFilter.all());
    }

    @ParameterizedTest
    @CsvSource({"true, UNREAD", "false, READ"})
    void search_givenRecipientAndReadState_searchesWhatTheRecipientCanSee(String unread, ReadFilter expected) {
        given(queries.find(any())).willReturn(List.of());

        assertThat(mvc.get().uri(NOTIFICATIONS).param(RECIPIENT_PARAM, RECIPIENT).param(UNREAD_PARAM, unread))
                .hasStatusOk();

        verify(queries).find(new NotificationFilter(new RecipientFilter.VisibleTo(RECIPIENT), expected));
    }

    @Test
    void search_givenResults_returnsThemInOrder() {
        var notification = NotificationFixtures.unread(new Audience.Person(RECIPIENT));
        given(queries.find(any())).willReturn(List.of(notification));

        assertThat(mvc.get().uri(NOTIFICATIONS))
                .bodyJson().extractingPath(FIRST_ID_PATH).isEqualTo(notification.id().toString());
    }

    @Test
    void search_givenTooLongRecipient_returnsBadRequest() {
        assertThat(mvc.get().uri(NOTIFICATIONS).param(RECIPIENT_PARAM, TOO_LONG_RECIPIENT))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void byId_givenBroadcast_returnsItWithoutRecipient() {
        var broadcast = NotificationFixtures.unread(new Audience.Everyone());
        given(queries.byId(broadcast.id())).willReturn(Optional.of(broadcast));

        assertThat(mvc.get().uri(NOTIFICATION, broadcast.id()))
                .hasStatusOk()
                .bodyJson().extractingPath(RECIPIENT_PATH).isNull();
    }

    @Test
    void byId_givenPersonalNotification_returnsRecipient() {
        var personal = NotificationFixtures.unread(new Audience.Person(RECIPIENT));
        given(queries.byId(personal.id())).willReturn(Optional.of(personal));

        assertThat(mvc.get().uri(NOTIFICATION, personal.id()))
                .bodyJson().extractingPath(RECIPIENT_PATH).isEqualTo(RECIPIENT);
    }

    @Test
    void byId_givenUnknownId_returnsNotFoundProblem() {
        given(queries.byId(UNKNOWN_ID)).willReturn(Optional.empty());

        assertThat(mvc.get().uri(NOTIFICATION, UNKNOWN_ID))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath(TYPE_PATH).isEqualTo(ApiExceptionHandler.NOT_FOUND_TYPE.toString());
    }

    @Test
    void byId_givenMalformedId_returnsBadRequest() {
        assertThat(mvc.get().uri(NOTIFICATION, MALFORMED_ID)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void markRead_givenKnownId_returnsNotificationWithReadTime() {
        var read = NotificationFixtures.read(new Audience.Person(RECIPIENT));
        given(readMarker.markRead(read.id())).willReturn(Optional.of(read));

        assertThat(mvc.put().uri(MARK_READ, read.id()))
                .hasStatusOk()
                .bodyJson().extractingPath(READ_AT_PATH).isEqualTo(NotificationFixtures.READ_AT.toString());
    }

    @Test
    void markRead_givenKnownId_returnsTheSameNotification() {
        var read = NotificationFixtures.read(new Audience.Person(RECIPIENT));
        given(readMarker.markRead(read.id())).willReturn(Optional.of(read));

        assertThat(mvc.put().uri(MARK_READ, read.id()))
                .bodyJson().extractingPath(ID_PATH).isEqualTo(read.id().toString());
    }

    @Test
    void markRead_givenUnknownId_returnsNotFound() {
        given(readMarker.markRead(UNKNOWN_ID)).willReturn(Optional.empty());

        assertThat(mvc.put().uri(MARK_READ, UNKNOWN_ID)).hasStatus(HttpStatus.NOT_FOUND);
    }
}
