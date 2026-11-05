package edu.demo.addtask.task.messaging;

import edu.demo.addtask.task.application.TaskCommands;
import jakarta.validation.Validator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: turns task commands from RabbitMQ into use-case calls.
 *
 * <p>An invalid command is rejected straight to the dead-letter queue. Any other failure, such as
 * an assignment arriving before the task's add command, propagates and is retried with backoff.
 */
@Component
class TaskCommandListener {

    private final TaskCommands commands;
    private final Validator validator;

    TaskCommandListener(TaskCommands commands, Validator validator) {
        this.commands = commands;
        this.validator = validator;
    }

    @RabbitListener(id = TasksTopology.ADD_TASK_LISTENER, queues = TasksTopology.ADD_TASK_QUEUE)
    void onAddTask(AddTaskCommand command) {
        requireValid(command);
        commands.addOnce(command.commandId(), command.toDraft());
    }

    @RabbitListener(id = TasksTopology.ASSIGN_TASK_LISTENER, queues = TasksTopology.ASSIGN_TASK_QUEUE)
    void onAssignTask(AssignTaskCommand command) {
        requireValid(command);
        commands.assign(command.toAssignment());
    }

    private void requireValid(Object command) {
        var violations = validator.validate(command);
        if (!violations.isEmpty()) {
            throw new InvalidCommandException(violations);
        }
    }
}
