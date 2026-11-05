import { requestJson } from '@/shared/lib/apiClient';
import { TaskListSchema, TaskSchema, type Assignment, type NewTask, type Task, type TaskId } from './task';

const TASKS_PATH = '/api/tasks';

export function fetchTasks(assignee: string | undefined): Promise<readonly Task[]> {
  return requestJson(TASKS_PATH, TaskListSchema, { query: { assignee } });
}

export function createTask(task: NewTask): Promise<Task> {
  return requestJson(TASKS_PATH, TaskSchema, { method: 'POST', body: task });
}

export function assignTask(id: TaskId, assignment: Assignment): Promise<Task> {
  return requestJson(`${TASKS_PATH}/${encodeURIComponent(id)}/assignee`, TaskSchema, {
    method: 'PUT',
    body: assignment,
  });
}
