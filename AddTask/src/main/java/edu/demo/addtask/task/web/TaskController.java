package edu.demo.addtask.task.web;

import edu.demo.addtask.task.application.TaskCommands;
import edu.demo.addtask.task.application.TaskQueries;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.domain.TaskNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * HTTP adapter for adding, assigning and reading tasks.
 */
@RestController
@RequestMapping(path = TaskController.TASKS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
class TaskController {

    static final String TASKS_PATH = "/api/tasks";

    private final TaskCommands commands;
    private final TaskQueries queries;

    TaskController(TaskCommands commands, TaskQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    @Operation(summary = "List tasks, newest first, optionally only those assigned to one person")
    @GetMapping
    List<TaskResponse> list(@RequestParam(defaultValue = "") String assignee) {
        return queries.find(new TaskFilter(assignee)).stream().map(TaskResponse::from).toList();
    }

    @Operation(summary = "Read one task")
    @ApiResponse(responseCode = "404", description = "No task with this id")
    @GetMapping("/{id}")
    TaskResponse byId(@PathVariable UUID id) {
        return queries.byId(id).map(TaskResponse::from).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Operation(summary = "Add a task, optionally assigning it straight away")
    @ApiResponse(responseCode = "201", description = "Created; Location points at the task")
    @ApiResponse(responseCode = "400", description = "Validation failed; errors lists each field")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        var task = commands.add(request.toDraft());
        return ResponseEntity.created(URI.create(TASKS_PATH + "/" + task.id())).body(TaskResponse.from(task));
    }

    @Operation(summary = "Assign or reassign a task; assigning the current assignee changes nothing")
    @ApiResponse(responseCode = "404", description = "No task with this id")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @PutMapping(path = "/{id}/assignee", consumes = MediaType.APPLICATION_JSON_VALUE)
    TaskResponse assign(@PathVariable UUID id, @Valid @RequestBody AssignTaskRequest request) {
        return TaskResponse.from(commands.assign(new TaskAssignment(id, request.assignee())));
    }
}
