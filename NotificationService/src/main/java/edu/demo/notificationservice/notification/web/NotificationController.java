package edu.demo.notificationservice.notification.web;

import edu.demo.notificationservice.notification.application.NotificationNotFoundException;
import edu.demo.notificationservice.notification.application.NotificationQueries;
import edu.demo.notificationservice.notification.application.NotificationReadMarker;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST adapter for reading notifications and marking them read.
 */
@RestController
@RequestMapping(path = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
class NotificationController {

    private final NotificationQueries queries;
    private final NotificationReadMarker readMarker;

    NotificationController(NotificationQueries queries, NotificationReadMarker readMarker) {
        this.queries = queries;
        this.readMarker = readMarker;
    }

    @GetMapping
    List<NotificationResponse> search(@Valid NotificationSearch search) {
        return queries.find(search.toFilter()).stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/{id}")
    NotificationResponse byId(@PathVariable UUID id) {
        return queries.byId(id)
                .map(NotificationResponse::from)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @PutMapping("/{id}/read")
    NotificationResponse markRead(@PathVariable UUID id) {
        return readMarker.markRead(id)
                .map(NotificationResponse::from)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
