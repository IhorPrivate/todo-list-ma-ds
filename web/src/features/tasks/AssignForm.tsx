import { useActionState } from 'react';
import { textField } from '@/shared/lib/formData';
import { Button } from '@/shared/ui/Button';
import { FormField } from '@/shared/ui/FormField';
import styles from './AssignForm.module.scss';
import { fieldErrorsFromSchema, fieldErrorsFromServer, type FieldErrors } from './formErrors';
import { ASSIGNEE_MAX_LENGTH, AssignmentSchema, taskMessages, type Task } from './task';
import { useAssignTask } from './useTasks';

type AssignState = {
  draft: string;
  errors: FieldErrors<'assignee'>;
  failed: boolean;
};

const FIELDS = ['assignee'] as const;
const INITIAL_STATE: AssignState = { draft: '', errors: {}, failed: false };

type AssignFormProps = {
  task: Task;
};

export function AssignForm({ task }: AssignFormProps) {
  const assignTask = useAssignTask();
  const [state, submit, pending] = useActionState(async (_previous: AssignState, formData: FormData) => {
    const draft = textField(formData, 'assignee');
    const parsed = AssignmentSchema.safeParse({ assignee: draft });
    if (!parsed.success) {
      return { draft, errors: fieldErrorsFromSchema(parsed.error, FIELDS), failed: false };
    }
    try {
      await assignTask.mutateAsync({ id: task.id, assignment: parsed.data });
      return INITIAL_STATE;
    } catch (error) {
      const errors = fieldErrorsFromServer(error, FIELDS);
      return { draft, errors, failed: Object.keys(errors).length === 0 };
    }
  }, INITIAL_STATE);

  return (
    <form action={submit} noValidate className={styles.form}>
      <div className={styles.field}>
        <FormField
          name="assignee"
          label={`Assignee for ${task.title}`}
          hideLabel
          placeholder={task.assignee ?? 'Assignee name'}
          maxLength={ASSIGNEE_MAX_LENGTH}
          defaultValue={state.draft}
          error={state.errors.assignee}
        />
      </div>
      <Button type="submit" variant="secondary" disabled={pending}>
        {task.assignee === null ? 'Assign' : 'Reassign'}
      </Button>
      {state.failed && (
        <p role="alert" className={styles.failure}>
          {taskMessages.saveFailed}
        </p>
      )}
    </form>
  );
}
