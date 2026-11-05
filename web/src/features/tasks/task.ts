import { z } from 'zod';

declare const brand: unique symbol;
type Brand<T, Name extends string> = T & { readonly [brand]: Name };

export type TaskId = Brand<string, 'TaskId'>;

function taskId(value: string): TaskId {
  // oxlint-disable-next-line typescript/no-unsafe-type-assertion -- the one sanctioned brand cast
  return value as TaskId;
}

export const TITLE_MAX_LENGTH = 200;
export const DESCRIPTION_MAX_LENGTH = 2000;
export const ASSIGNEE_MAX_LENGTH = 100;

export const taskMessages = {
  titleRequired: 'Title is required',
  titleTooLong: `Title must be at most ${TITLE_MAX_LENGTH} characters`,
  descriptionTooLong: `Description must be at most ${DESCRIPTION_MAX_LENGTH} characters`,
  assigneeRequired: 'Assignee is required',
  assigneeTooLong: `Assignee must be at most ${ASSIGNEE_MAX_LENGTH} characters`,
  loadFailed: 'Could not load tasks.',
  saveFailed: 'Could not save the task. Try again.',
  noTasks: 'No tasks yet. Add the first one above.',
  noMatchingTasks: 'No tasks match this filter.',
  unassigned: 'Unassigned',
  assignedTo: (assignee: string) => `Assigned to ${assignee}`,
  created: (title: string) => `Task "${title}" added`,
} as const;

const instant = z.iso.datetime({ offset: true }).transform((value) => new Date(value));

export const TaskSchema = z.object({
  id: z.uuid().transform(taskId),
  title: z.string(),
  description: z.string().nullable(),
  assignee: z.string().nullable(),
  createdAt: instant,
  assignedAt: instant.nullable(),
});

export const TaskListSchema = z.array(TaskSchema).readonly();

export type Task = z.output<typeof TaskSchema>;
export type TaskWire = z.input<typeof TaskSchema>;

const optionalText = (maxLength: number, tooLong: string) =>
  z
    .string()
    .trim()
    .max(maxLength, tooLong)
    .transform((value) => (value === '' ? null : value));

/** What the add-task form submits, already shaped as the `POST /api/tasks` body. */
export const NewTaskSchema = z.object({
  title: z.string().trim().min(1, taskMessages.titleRequired).max(TITLE_MAX_LENGTH, taskMessages.titleTooLong),
  description: optionalText(DESCRIPTION_MAX_LENGTH, taskMessages.descriptionTooLong),
  assignee: optionalText(ASSIGNEE_MAX_LENGTH, taskMessages.assigneeTooLong),
});

export type NewTask = z.output<typeof NewTaskSchema>;
export type NewTaskField = keyof NewTask;

/** What the assign form submits, shaped as the `PUT /api/tasks/{id}/assignee` body. */
export const AssignmentSchema = z.object({
  assignee: z
    .string()
    .trim()
    .min(1, taskMessages.assigneeRequired)
    .max(ASSIGNEE_MAX_LENGTH, taskMessages.assigneeTooLong),
});

export type Assignment = z.output<typeof AssignmentSchema>;
