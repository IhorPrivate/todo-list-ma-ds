import type { Task } from './task';

export type TaskFilter =
  | { kind: 'all' }
  | { kind: 'unassigned' }
  | { kind: 'assignee'; assignee: string };

const VIEW_PARAM = 'view';
const ASSIGNEE_PARAM = 'assignee';
const UNASSIGNED_VIEW = 'unassigned';

export function filterFrom(params: URLSearchParams): TaskFilter {
  if (params.get(VIEW_PARAM) === UNASSIGNED_VIEW) {
    return { kind: 'unassigned' };
  }
  const assignee = params.get(ASSIGNEE_PARAM)?.trim() ?? '';
  return assignee === '' ? { kind: 'all' } : { kind: 'assignee', assignee };
}

export function paramsFor(filter: TaskFilter): URLSearchParams {
  switch (filter.kind) {
    case 'all':
      return new URLSearchParams();
    case 'unassigned':
      return new URLSearchParams({ [VIEW_PARAM]: UNASSIGNED_VIEW });
    case 'assignee':
      return new URLSearchParams({ [ASSIGNEE_PARAM]: filter.assignee });
    default:
      return assertNever(filter);
  }
}

/** The assignee the server should filter by; unassigned filtering happens client-side. */
export function serverAssignee(filter: TaskFilter): string | undefined {
  return filter.kind === 'assignee' ? filter.assignee : undefined;
}

export function matches(filter: TaskFilter, task: Task): boolean {
  return filter.kind !== 'unassigned' || task.assignee === null;
}

function assertNever(value: never): never {
  throw new Error(`Unhandled filter: ${JSON.stringify(value)}`);
}
