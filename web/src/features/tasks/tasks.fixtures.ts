import type { TaskWire } from './task';

export const ALICE = 'Alice';
export const BOB = 'Bob';

export const unassignedTask: TaskWire = {
  id: '3f1c2a9e-8b4d-4c6e-9a1f-0d2b3c4e5f60',
  title: 'Write release notes',
  description: 'Cover the RabbitMQ changes',
  assignee: null,
  createdAt: '2026-11-01T09:00:00Z',
  assignedAt: null,
};

export const aliceTask: TaskWire = {
  id: '7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d',
  title: 'Review the AddTask API',
  description: null,
  assignee: ALICE,
  createdAt: '2026-11-02T10:30:00.123456Z',
  assignedAt: '2026-11-02T11:00:00Z',
};

export const bobTask: TaskWire = {
  id: 'c1d2e3f4-a5b6-4c7d-9e8f-a0b1c2d3e4f5',
  title: 'Configure the dead-letter queue',
  description: null,
  assignee: BOB,
  createdAt: '2026-11-03T08:15:00Z',
  assignedAt: '2026-11-03T08:16:00Z',
};

export const allTasks: readonly TaskWire[] = [unassignedTask, aliceTask, bobTask];

export const NEW_TASK_ID = '9e8d7c6b-5a49-4382-a716-151413121110';
export const NEW_TASK_CREATED_AT = '2026-11-03T12:00:00Z';
export const ASSIGNED_AT = '2026-11-03T12:05:00Z';

export const newTaskDraft = {
  title: 'Prepare the demo',
  assignee: ALICE,
} as const;

export const duplicateTitleRejection = {
  type: 'urn:problem:validation',
  title: 'Bad Request',
  status: 400,
  detail: 'Validation failed',
  errors: [{ field: 'title', message: 'A task with this title already exists' }],
} as const;

export const serverFailure = {
  type: 'about:blank',
  title: 'Internal Server Error',
  status: 500,
} as const;
