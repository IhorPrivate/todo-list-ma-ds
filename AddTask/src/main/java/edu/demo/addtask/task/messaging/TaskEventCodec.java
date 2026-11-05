package edu.demo.addtask.task.messaging;

import edu.demo.addtask.task.event.TaskAdded;
import edu.demo.addtask.task.event.TaskAssigned;
import edu.demo.addtask.task.event.TaskEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps events to their routing key and JSON body on {@code tasks.events}.
 */
@Component
public class TaskEventCodec {

    private final JsonMapper jsonMapper;

    TaskEventCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * @param event an event
     * @return its routing key
     */
    public String routingKey(TaskEvent event) {
        return switch (event) {
            case TaskAdded ignored -> TasksTopology.TASK_ADDED_KEY;
            case TaskAssigned ignored -> TasksTopology.TASK_ASSIGNED_KEY;
        };
    }

    /**
     * @param event an event
     * @return its JSON body
     */
    public String toJson(TaskEvent event) {
        return jsonMapper.writeValueAsString(event);
    }
}
