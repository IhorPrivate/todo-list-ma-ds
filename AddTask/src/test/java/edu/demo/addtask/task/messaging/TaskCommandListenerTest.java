package edu.demo.addtask.task.messaging;

import edu.demo.addtask.support.TestMessages;
import edu.demo.addtask.task.application.TaskCommands;
import edu.demo.addtask.task.domain.TaskAssignment;
import edu.demo.addtask.task.domain.TaskDraft;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TaskCommandListenerTest {

    private static final UUID COMMAND_ID = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final Instant REQUESTED = Instant.parse("2026-11-03T10:00:00Z");
    private static final String TITLE = "Write release notes";
    private static final String BLANK = " ";
    private static final String NO_TEXT = "";
    private static final String BOB = "bob";
    private static final ValidatorFactory VALIDATION = Validation.buildDefaultValidatorFactory();

    private TaskCommands commands;
    private TaskCommandListener listener;

    @BeforeEach
    void setUp() {
        commands = mock(TaskCommands.class);
        listener = new TaskCommandListener(commands, VALIDATION.getValidator());
    }

    @AfterAll
    static void closeValidation() {
        VALIDATION.close();
    }

    @Test
    void onAddTask_givenValidCommand_addsOnceUnderCommandId() {
        listener.onAddTask(new AddTaskCommand(COMMAND_ID, TITLE, null, null, REQUESTED));

        verify(commands).addOnce(COMMAND_ID, new TaskDraft(TITLE, NO_TEXT, NO_TEXT));
    }

    @Test
    void onAddTask_givenBlankTitle_rejectsWithoutCallingUseCase() {
        assertThatExceptionOfType(InvalidCommandException.class)
                .isThrownBy(() -> listener.onAddTask(new AddTaskCommand(COMMAND_ID, BLANK, null, null, REQUESTED)))
                .withMessage(TestMessages.get("command.invalid-title"));
        verifyNoInteractions(commands);
    }

    @Test
    void onAssignTask_givenValidCommand_assigns() {
        listener.onAssignTask(new AssignTaskCommand(COMMAND_ID, TASK_ID, BOB, REQUESTED));

        verify(commands).assign(new TaskAssignment(TASK_ID, BOB));
    }

    @Test
    void onAssignTask_givenMissingTaskId_rejects() {
        assertThatExceptionOfType(InvalidCommandException.class)
                .isThrownBy(() -> listener.onAssignTask(new AssignTaskCommand(COMMAND_ID, null, BOB, REQUESTED)));
    }
}
