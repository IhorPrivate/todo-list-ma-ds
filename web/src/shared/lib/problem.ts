import { z } from 'zod';

const FieldErrorSchema = z.object({
  field: z.string(),
  message: z.string(),
});

export const ProblemSchema = z.object({
  type: z.string().optional(),
  title: z.string().optional(),
  status: z.number().int(),
  detail: z.string().optional(),
  errors: z.array(FieldErrorSchema).optional(),
});

export type Problem = z.output<typeof ProblemSchema>;
export type FieldError = z.output<typeof FieldErrorSchema>;

export class ApiError extends Error {
  readonly status: number;
  readonly problem: Problem | undefined;

  constructor(status: number, problem: Problem | undefined) {
    super(problem?.detail ?? problem?.title ?? `Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }

  fieldErrors(): readonly FieldError[] {
    return this.problem?.errors ?? [];
  }
}
