import type { NotificationWire } from './notification';

export const ALICE = 'Alice';

export const broadcastNotification: NotificationWire = {
  id: '0b1c2d3e-4f50-4a61-8b72-c3d4e5f60718',
  type: 'TASK_ADDED',
  recipient: null,
  message: 'New task "Write release notes" is waiting for an assignee',
  taskId: '3f1c2a9e-8b4d-4c6e-9a1f-0d2b3c4e5f60',
  createdAt: '2026-11-01T09:00:01Z',
  readAt: null,
};

export const aliceAssignedNotification: NotificationWire = {
  id: '1c2d3e4f-5061-4b72-9c83-d4e5f6071829',
  type: 'TASK_ASSIGNED',
  recipient: ALICE,
  message: 'You were assigned "Review the AddTask API"',
  taskId: '7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d',
  createdAt: '2026-11-02T11:00:01.123Z',
  readAt: null,
};

export const readNotification: NotificationWire = {
  id: '2d3e4f50-6172-4c83-8d94-e5f60718293a',
  type: 'TASK_UNASSIGNED',
  recipient: 'Bob',
  message: '"Configure the dead-letter queue" was reassigned to Alice',
  taskId: 'c1d2e3f4-a5b6-4c7d-9e8f-a0b1c2d3e4f5',
  createdAt: '2026-11-03T08:16:01Z',
  readAt: '2026-11-03T09:00:00Z',
};

export const allNotifications: readonly NotificationWire[] = [
  broadcastNotification,
  aliceAssignedNotification,
  readNotification,
];

export const READ_AT = '2026-11-03T12:10:00Z';

export const serverFailure = {
  type: 'about:blank',
  title: 'Internal Server Error',
  status: 500,
} as const;
