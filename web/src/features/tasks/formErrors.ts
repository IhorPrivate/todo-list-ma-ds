import { z } from 'zod';
import { ApiError } from '@/shared/lib/problem';

export type FieldErrors<Field extends string> = Partial<Record<Field, string>>;

function collect<Field extends string>(
  fields: readonly Field[],
  messageFor: (field: Field) => string | undefined,
): FieldErrors<Field> {
  const errors: FieldErrors<Field> = {};
  for (const field of fields) {
    const message = messageFor(field);
    if (message !== undefined) {
      errors[field] = message;
    }
  }
  return errors;
}

/** First message per field from a failed zod parse. */
export function fieldErrorsFromSchema<Field extends string>(
  error: z.ZodError,
  fields: readonly Field[],
): FieldErrors<Field> {
  const fieldErrors: Partial<Record<string, string[]>> = z.flattenError(error).fieldErrors;
  return collect(fields, (field) => fieldErrors[field]?.[0]);
}

/** Field messages from a 400 problem, keeping only the fields this form renders. */
export function fieldErrorsFromServer<Field extends string>(
  error: unknown,
  fields: readonly Field[],
): FieldErrors<Field> {
  if (!(error instanceof ApiError)) {
    return {};
  }
  const serverErrors = error.fieldErrors();
  return collect(fields, (field) => serverErrors.find((fieldError) => fieldError.field === field)?.message);
}
