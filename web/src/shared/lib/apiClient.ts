import type { z } from 'zod';
import { ApiError, ProblemSchema, type Problem } from './problem';

type JsonRequest = {
  method?: 'GET' | 'POST' | 'PUT';
  query?: Readonly<Record<string, string | undefined>>;
  body?: unknown;
};

function urlFor(path: string, query: JsonRequest['query']): URL {
  const url = new URL(path, window.location.origin);
  for (const [name, value] of Object.entries(query ?? {})) {
    if (value !== undefined) {
      url.searchParams.set(name, value);
    }
  }
  return url;
}

async function problemFrom(response: Response): Promise<Problem | undefined> {
  const parsed = ProblemSchema.safeParse(await response.json().catch(() => undefined));
  return parsed.success ? parsed.data : undefined;
}

/**
 * Sends a JSON request and parses the response body with `schema`.
 * Non-2xx responses throw {@link ApiError} carrying the RFC 9457 problem body when present.
 */
export async function requestJson<Schema extends z.ZodType>(
  path: string,
  schema: Schema,
  request: JsonRequest = {},
): Promise<z.output<Schema>> {
  const response = await fetch(urlFor(path, request.query), {
    method: request.method ?? 'GET',
    headers: {
      Accept: 'application/json, application/problem+json',
      ...(request.body === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    ...(request.body === undefined ? {} : { body: JSON.stringify(request.body) }),
  });
  if (!response.ok) {
    throw new ApiError(response.status, await problemFrom(response));
  }
  return schema.parse(await response.json());
}
