package edu.demo.notificationservice.notification.web;

import edu.demo.notificationservice.notification.application.NotificationNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Map;

/**
 * Translates failures into RFC 9457 problem details. The only place that maps exceptions to HTTP statuses.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final URI NOT_FOUND_TYPE = URI.create("urn:problem:not-found");
    private static final String ERRORS_PROPERTY = "errors";
    private static final String FIELD = "field";
    private static final String MESSAGE = "message";

    @ExceptionHandler(NotificationNotFoundException.class)
    ProblemDetail notFound(NotificationNotFoundException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(NOT_FOUND_TYPE);
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = exception.getBody();
        problem.setProperty(ERRORS_PROPERTY, exception.getFieldErrors().stream()
                .map(error -> Map.of(FIELD, error.getField(), MESSAGE, String.valueOf(error.getDefaultMessage())))
                .toList());
        return handleExceptionInternal(exception, problem, headers, status, request);
    }
}
