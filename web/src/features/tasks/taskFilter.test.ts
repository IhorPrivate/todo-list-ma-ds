import { describe, expect, it } from 'vitest';
import { filterFrom, paramsFor, type TaskFilter } from './taskFilter';
import { ALICE } from './tasks.fixtures';

const BLANK_ASSIGNEE = '   ';

describe('taskFilter', () => {
  it.each<TaskFilter>([{ kind: 'all' }, { kind: 'unassigned' }, { kind: 'assignee', assignee: ALICE }])(
    'round-trips %o through the URL',
    (filter) => {
      expect(filterFrom(paramsFor(filter))).toEqual(filter);
    },
  );

  it('treats a blank assignee as no filter', () => {
    expect(filterFrom(new URLSearchParams({ assignee: BLANK_ASSIGNEE }))).toEqual({ kind: 'all' });
  });
});
