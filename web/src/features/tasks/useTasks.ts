import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { assignTask, createTask, fetchTasks } from './tasks.api';
import type { Assignment, TaskId } from './task';
import { matches, serverAssignee, type TaskFilter } from './taskFilter';

export const taskKeys = {
  all: ['tasks'] as const,
  list: (assignee: string | undefined) => [...taskKeys.all, 'list', assignee ?? null] as const,
};

export function useTasks(filter: TaskFilter) {
  const assignee = serverAssignee(filter);
  return useQuery({
    queryKey: taskKeys.list(assignee),
    queryFn: () => fetchTasks(assignee),
    select: (tasks) => tasks.filter((task) => matches(filter, task)),
  });
}

export function useCreateTask() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createTask,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: taskKeys.all }),
  });
}

export function useAssignTask() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, assignment }: { id: TaskId; assignment: Assignment }) => assignTask(id, assignment),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: taskKeys.all }),
  });
}
