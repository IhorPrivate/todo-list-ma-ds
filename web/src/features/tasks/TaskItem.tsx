import { useId } from 'react';
import { AssignForm } from './AssignForm';
import styles from './TaskItem.module.scss';
import { taskMessages, type Task } from './task';

const createdFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });

type TaskItemProps = {
  task: Task;
};

export function TaskItem({ task }: TaskItemProps) {
  const titleId = useId();
  return (
    <li className={styles.item} aria-labelledby={titleId}>
      <div className={styles.body}>
        <h3 id={titleId} className={styles.title}>
          {task.title}
        </h3>
        {task.description !== null && <p className={styles.description}>{task.description}</p>}
        <p className={styles.meta}>
          <span className={styles.assignee} data-assigned={task.assignee !== null}>
            {task.assignee === null ? taskMessages.unassigned : taskMessages.assignedTo(task.assignee)}
          </span>
          <time dateTime={task.createdAt.toISOString()}>Created {createdFormat.format(task.createdAt)}</time>
        </p>
      </div>
      <AssignForm task={task} />
    </li>
  );
}
