import { useActionState, useId } from 'react';
import { textField } from '@/shared/lib/formData';
import { Button } from '@/shared/ui/Button';
import { FormField } from '@/shared/ui/FormField';
import styles from './AddTaskForm.module.scss';
import { fieldErrorsFromSchema, fieldErrorsFromServer, type FieldErrors } from './formErrors';
import {
  ASSIGNEE_MAX_LENGTH,
  DESCRIPTION_MAX_LENGTH,
  NewTaskSchema,
  TITLE_MAX_LENGTH,
  taskMessages,
  type NewTask,
  type NewTaskField,
  type Task,
} from './task';
import { useCreateTask } from './useTasks';

const FIELDS: readonly NewTaskField[] = ['title', 'description', 'assignee'];

type Draft = Record<NewTaskField, string>;

type AddTaskState = {
  draft: Draft;
  errors: FieldErrors<NewTaskField>;
  failed: boolean;
  createdTitle: string | undefined;
};

const EMPTY_DRAFT: Draft = { title: '', description: '', assignee: '' };
const INITIAL_STATE: AddTaskState = { draft: EMPTY_DRAFT, errors: {}, failed: false, createdTitle: undefined };

async function submitNewTask(formData: FormData, create: (task: NewTask) => Promise<Task>): Promise<AddTaskState> {
  const draft: Draft = {
    title: textField(formData, 'title'),
    description: textField(formData, 'description'),
    assignee: textField(formData, 'assignee'),
  };
  const parsed = NewTaskSchema.safeParse(draft);
  if (!parsed.success) {
    return { draft, errors: fieldErrorsFromSchema(parsed.error, FIELDS), failed: false, createdTitle: undefined };
  }
  try {
    const created = await create(parsed.data);
    return { ...INITIAL_STATE, createdTitle: created.title };
  } catch (error) {
    const errors = fieldErrorsFromServer(error, FIELDS);
    return { draft, errors, failed: Object.keys(errors).length === 0, createdTitle: undefined };
  }
}

export function AddTaskForm() {
  const headingId = useId();
  const createTask = useCreateTask();
  const [state, submit, pending] = useActionState(
    (_previous: AddTaskState, formData: FormData) => submitNewTask(formData, createTask.mutateAsync),
    INITIAL_STATE,
  );

  return (
    <form action={submit} noValidate className={styles.form} aria-labelledby={headingId}>
      <h2 id={headingId} className={styles.heading}>
        New task
      </h2>
      <div className={styles.row}>
        <FormField
          name="title"
          label="Title"
          required
          maxLength={TITLE_MAX_LENGTH}
          defaultValue={state.draft.title}
          error={state.errors.title}
        />
        <FormField
          name="assignee"
          label="Assignee"
          placeholder="Optional"
          maxLength={ASSIGNEE_MAX_LENGTH}
          defaultValue={state.draft.assignee}
          error={state.errors.assignee}
        />
      </div>
      <FormField
        name="description"
        label="Description"
        multiline
        placeholder="Optional"
        maxLength={DESCRIPTION_MAX_LENGTH}
        defaultValue={state.draft.description}
        error={state.errors.description}
      />
      <div className={styles.actions}>
        <Button type="submit" disabled={pending}>
          {pending ? 'Adding…' : 'Add task'}
        </Button>
        <p role="status" className={styles.success}>
          {state.createdTitle === undefined ? '' : taskMessages.created(state.createdTitle)}
        </p>
        {state.failed && (
          <p role="alert" className={styles.failure}>
            {taskMessages.saveFailed}
          </p>
        )}
      </div>
    </form>
  );
}
