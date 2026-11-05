package edu.demo.addtask.task.application;

import edu.demo.addtask.task.domain.Task;
import edu.demo.addtask.task.domain.TaskFilter;
import edu.demo.addtask.task.event.TaskEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Test double for {@link TaskStore} that records every stored event.
 */
class InMemoryTaskStore implements TaskStore {

    private final Map<UUID, Task> tasks = new LinkedHashMap<>();
    private final List<TaskEvent> events = new ArrayList<>();

    @Override
    public boolean insert(Task task, TaskEvent event) {
        if (tasks.containsKey(task.id())) {
            return false;
        }
        tasks.put(task.id(), task);
        events.add(event);
        return true;
    }

    @Override
    public void update(Task task, TaskEvent event) {
        tasks.put(task.id(), task);
        events.add(event);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> find(TaskFilter filter) {
        return tasks.values().stream()
                .filter(task -> filter.requiredAssignee().map(task::isAssignedTo).orElse(true))
                .sorted(Comparator.comparing(Task::createdAt).reversed())
                .toList();
    }

    List<TaskEvent> events() {
        return List.copyOf(events);
    }
}
