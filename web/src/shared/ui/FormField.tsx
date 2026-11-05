import { useId } from 'react';
import styles from './FormField.module.scss';

type FormFieldProps = {
  name: string;
  label: string;
  defaultValue?: string;
  error?: string | undefined;
  multiline?: boolean;
  hideLabel?: boolean;
  maxLength?: number;
  required?: boolean;
  placeholder?: string;
};

export function FormField({
  name,
  label,
  defaultValue = '',
  error,
  multiline = false,
  hideLabel = false,
  maxLength,
  required = false,
  placeholder,
}: FormFieldProps) {
  const id = useId();
  const errorId = `${id}-error`;
  const controlProps = {
    id,
    name,
    defaultValue,
    className: styles.control,
    'aria-invalid': error !== undefined,
    ...(error === undefined ? {} : { 'aria-describedby': errorId }),
    ...(maxLength === undefined ? {} : { maxLength }),
    ...(placeholder === undefined ? {} : { placeholder }),
    required,
  };
  return (
    <div className={styles.field}>
      <label htmlFor={id} className={hideLabel ? styles.labelHidden : styles.label}>
        {label}
      </label>
      {multiline ? <textarea rows={3} {...controlProps} /> : <input type="text" {...controlProps} />}
      {error !== undefined && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  );
}
