package edu.demo.addtask.task.web;

import edu.demo.addtask.support.Fixtures;
import edu.demo.addtask.task.application.TaskCommands;
import edu.demo.addtask.task.application.TaskQueries;
import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskDraft;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.domain.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    // Paths and fixtures
    private static final String TASKS = "/api/tasks";
    private static final String TASK = "/api/tasks/{id}";
    private static final String ASSIGNEE = "/api/tasks/{id}/assignee";
    private static final String CREATE_VALID = "tasks/create-valid.json";
    private static final String ASSIGN_VALID = "tasks/assign-valid.json";
    private static final String ASSIGN_BLANK = "tasks/assign-blank.json";

    // JSON paths
    private static final String ID_PATH = "$.id";
    private static final String ASSIGNEE_PATH = "$.assignee";
    private static final String FIRST_ERROR_FIELD = "$.errors[0].field";
    private static final String FIRST_ITEM_ID = "$[0].id";

    // Values
    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final Instant CREATED = Instant.parse("2026-11-03T10:00:00Z");
    private static final String TITLE = "Write release notes";
    private static final String DESCRIPTION = "Cover the RabbitMQ changes";
    private static final String ALICE = "alice";
    private static final String BOB = "bob";
    private static final String ASSIGNEE_PARAM = "assignee";
    private static final String CREATED_LOCATION = "/api/tasks/" + TASK_ID;
    private static final TaskDraft VALID_DRAFT = new TaskDraft(TITLE, DESCRIPTION, ALICE);
    private static final Task ALICES_TASK = Task.create(TASK_ID, VALID_DRAFT, CREATED);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private TaskCommands commands;

    @MockitoBean
    private TaskQueries queries;

    @Test
    void create_givenValidBody_returnsCreatedWithLocation() {
        given(commands.add(VALID_DRAFT)).willReturn(ALICES_TASK);

        var result = mvc.post().uri(TASKS).contentType(MediaType.APPLICATION_JSON).content(Fixtures.read(CREATE_VALID));

        assertThat(result).hasStatus(HttpStatus.CREATED).hasHeader(HttpHeaders.LOCATION, CREATED_LOCATION);
        assertThat(result).bodyJson().extractingPath(ID_PATH).isEqualTo(TASK_ID.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "tasks/create-blank-title.json, title",
            "tasks/create-title-too-long.json, title",
            "tasks/create-assignee-too-long.json, assignee"})
    void create_givenInvalidBody_returnsBadRequestNamingTheField(String fixture, String field) {
        var result = mvc.post().uri(TASKS).contentType(MediaType.APPLICATION_JSON).content(Fixtures.read(fixture));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath(FIRST_ERROR_FIELD).isEqualTo(field);
    }

    @Test
    void byId_givenExistingTask_returnsIt() {
        given(queries.byId(TASK_ID)).willReturn(Optional.of(ALICES_TASK));

        assertThat(mvc.get().uri(TASK, TASK_ID)).hasStatusOk()
                .bodyJson().extractingPath(ASSIGNEE_PATH).isEqualTo(ALICE);
    }

    @Test
    void byId_givenUnknownTask_returnsNotFoundProblem() {
        given(queries.byId(TASK_ID)).willReturn(Optional.empty());

        assertThat(mvc.get().uri(TASK, TASK_ID)).hasStatus(HttpStatus.NOT_FOUND)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void list_givenAssigneeParam_filtersByAssignee() {
        given(queries.find(new TaskFilter(ALICE))).willReturn(List.of(ALICES_TASK));

        assertThat(mvc.get().uri(TASKS).param(ASSIGNEE_PARAM, ALICE)).hasStatusOk()
                .bodyJson().extractingPath(FIRST_ITEM_ID).isEqualTo(TASK_ID.toString());
    }

    @Test
    void assign_givenValidBody_returnsReassignedTask() {
        given(commands.assign(new TaskAssignment(TASK_ID, BOB))).willReturn(ALICES_TASK.assignTo(BOB, CREATED));

        var result = mvc.put().uri(ASSIGNEE, TASK_ID).contentType(MediaType.APPLICATION_JSON)
                .content(Fixtures.read(ASSIGN_VALID));

        assertThat(result).hasStatusOk().bodyJson().extractingPath(ASSIGNEE_PATH).isEqualTo(BOB);
    }

    @Test
    void assign_givenUnknownTask_returnsNotFound() {
        willThrow(new TaskNotFoundException(TASK_ID)).given(commands).assign(any());

        var result = mvc.put().uri(ASSIGNEE, TASK_ID).contentType(MediaType.APPLICATION_JSON)
                .content(Fixtures.read(ASSIGN_VALID));

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void assign_givenBlankAssignee_returnsBadRequest() {
        var result = mvc.put().uri(ASSIGNEE, TASK_ID).contentType(MediaType.APPLICATION_JSON)
                .content(Fixtures.read(ASSIGN_BLANK));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
