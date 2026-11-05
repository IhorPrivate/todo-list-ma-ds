import { textField } from '@/shared/lib/formData';
import { Button } from '@/shared/ui/Button';
import { FormField } from '@/shared/ui/FormField';
import styles from './TaskFilterBar.module.scss';
import type { TaskFilter } from './taskFilter';

type TaskFilterBarProps = {
  filter: TaskFilter;
  onChange: (filter: TaskFilter) => void;
};

export function TaskFilterBar({ filter, onChange }: TaskFilterBarProps) {
  const currentAssignee = filter.kind === 'assignee' ? filter.assignee : '';

  function applyAssignee(formData: FormData) {
    const assignee = textField(formData, 'assignee');
    onChange(assignee === '' ? { kind: 'all' } : { kind: 'assignee', assignee });
  }

  return (
    <div className={styles.bar}>
      <div role="group" aria-label="Show tasks" className={styles.views}>
        <Button variant="secondary" aria-pressed={filter.kind === 'all'} onClick={() => onChange({ kind: 'all' })}>
          All
        </Button>
        <Button
          variant="secondary"
          aria-pressed={filter.kind === 'unassigned'}
          onClick={() => onChange({ kind: 'unassigned' })}
        >
          Unassigned
        </Button>
      </div>
      <form key={currentAssignee} action={applyAssignee} className={styles.assigneeFilter} role="search">
        <FormField name="assignee" label="Filter by assignee" defaultValue={currentAssignee} />
        <Button type="submit" variant="secondary">
          Apply
        </Button>
      </form>
    </div>
  );
}
