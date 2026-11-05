import { http, HttpResponse } from 'msw';
import { z } from 'zod';
import type { TaskWire } from './task';
import { ASSIGNED_AT, NEW_TASK_CREATED_AT, NEW_TASK_ID } from './tasks.fixtures';

const TASKS_URL = '/api/tasks';
const ASSIGNEE_URL = '/api/tasks/:id/assignee';

const CreateBodySchema = z.object({
  title: z.string(),
  description: z.string().nullable(),
  assignee: z.string().nullable(),
});

const AssignBodySchema = z.object({ assignee: z.string() });

/** An in-memory AddTask API: requests made through it change what later requests return. */
export function fakeTaskApi(initial: readonly TaskWire[]) {
  const tasks: TaskWire[] = [...initial];
  const assigneeQueries: (string | null)[] = [];
  const createdBodies: z.output<typeof CreateBodySchema>[] = [];

  const handlers = [
    http.get(TASKS_URL, ({ request }) => {
      const assignee = new URL(request.url).searchParams.get('assignee');
      assigneeQueries.push(assignee);
      return HttpResponse.json(assignee === null ? tasks : tasks.filter((task) => task.assignee === assignee));
    }),
    http.post(TASKS_URL, async ({ request }) => {
      const body = CreateBodySchema.parse(await request.json());
      createdBodies.push(body);
      const task: TaskWire = {
        id: NEW_TASK_ID,
        ...body,
        createdAt: NEW_TASK_CREATED_AT,
        assignedAt: body.assignee === null ? null : NEW_TASK_CREATED_AT,
      };
      tasks.push(task);
      return HttpResponse.json(task, { status: 201, headers: { Location: `${TASKS_URL}/${task.id}` } });
    }),
    http.put(ASSIGNEE_URL, async ({ params, request }) => {
      const { assignee } = AssignBodySchema.parse(await request.json());
      const index = tasks.findIndex((task) => task.id === params['id']);
      const existing = tasks[index];
      if (existing === undefined) {
        return HttpResponse.json({ status: 404, title: 'Not Found' }, { status: 404 });
      }
      const updated: TaskWire = { ...existing, assignee, assignedAt: ASSIGNED_AT };
      tasks[index] = updated;
      return HttpResponse.json(updated);
    }),
  ];

  return { handlers, assigneeQueries, createdBodies };
}

export function problemResponse(url: 'list' | 'create', problem: { status: number }) {
  const respond = () =>
    HttpResponse.json(problem, { status: problem.status, headers: { 'Content-Type': 'application/problem+json' } });
  return url === 'list' ? http.get(TASKS_URL, respond) : http.post(TASKS_URL, respond);
}
