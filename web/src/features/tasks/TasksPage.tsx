import { useSearchParams } from '@/shared/lib/searchParams';
import { Button } from '@/shared/ui/Button';
import { AddTaskForm } from './AddTaskForm';
import { TaskFilterBar } from './TaskFilterBar';
import { TaskItem } from './TaskItem';
import styles from './TasksPage.module.scss';
import { taskMessages } from './task';
import { filterFrom, paramsFor, type TaskFilter } from './taskFilter';
import { useTasks } from './useTasks';

export function TasksPage() {
  const [params, navigate] = useSearchParams();
  const filter = filterFrom(params);

  return (
    <main className={styles.page}>
      <h1 className={styles.title}>Tasks</h1>
      <AddTaskForm />
      <section className={styles.section} aria-label="Task list">
        <TaskFilterBar filter={filter} onChange={(next) => navigate(paramsFor(next))} />
        <TaskListContent filter={filter} />
      </section>
    </main>
  );
}

function TaskListContent({ filter }: { filter: TaskFilter }) {
  const tasks = useTasks(filter);

  if (tasks.isPending) {
    return (
      <p role="status" className={styles.state}>
        Loading tasks…
      </p>
    );
  }
  if (tasks.isError) {
    return (
      <div role="alert" className={styles.alert}>
        <span>{taskMessages.loadFailed}</span>
        <Button variant="secondary" onClick={() => void tasks.refetch()}>
          Retry
        </Button>
      </div>
    );
  }
  if (tasks.data.length === 0) {
    return <p className={styles.state}>{filter.kind === 'all' ? taskMessages.noTasks : taskMessages.noMatchingTasks}</p>;
  }
  return (
    <ul role="list" aria-label="Tasks" className={styles.list}>
      {tasks.data.map((task) => (
        <TaskItem key={task.id} task={task} />
      ))}
    </ul>
  );
}
