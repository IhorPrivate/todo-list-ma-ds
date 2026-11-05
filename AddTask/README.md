# AddTask

Owns: adding tasks and assigning them. System of record for the `tasks` MongoDB database.

## Runs on
Java 25, Spring Boot 4.1. Port 8082.

```bash
docker compose -f ../docker/compose.infra.yaml up -d   # broker + database
mvn spring-boot:run
```

Tests: `mvn test`. The two end-to-end tests use Testcontainers and are skipped when Docker is not running.

## Contracts

**REST** (OpenAPI at `/v3/api-docs`, UI at `/swagger-ui.html`)

| Operation | Success | Errors |
|---|---|---|
| `GET /api/tasks[?assignee=name]` | 200, newest first | |
| `GET /api/tasks/{id}` | 200 | 404 |
| `POST /api/tasks` `{title, description?, assignee?}` | 201 + `Location` | 400 with `errors[{field, message}]` |
| `PUT /api/tasks/{id}/assignee` `{assignee}` | 200 | 400, 404 |

Errors are RFC 9457 problem details.

**Consumes** commands from exchange `tasks.commands` (direct):

| Key | Queue | Body |
|---|---|---|
| `task.add` | `addtask.add-task` | `AddTaskCommand {commandId, title, description?, assignee?, requestedAt}` |
| `task.assign` | `addtask.assign-task` | `AssignTaskCommand {commandId, taskId, assignee, requestedAt}` |

Both are safe to redeliver: `commandId` becomes the task id, and re-assigning the current assignee is a no-op. Invalid commands go straight to `<queue>.dlq` via `addtask.dead-letters`; other failures retry three times with backoff first.

**Publishes** events on exchange `tasks.events` (topic), with the AMQP message id set to `eventId`:

| Key | Body |
|---|---|
| `task.added` | `TaskAdded {eventId, occurredAt, taskId, title, description?, assignee?}` |
| `task.assigned` | `TaskAssigned {eventId, occurredAt, taskId, title, assignee, previousAssignee?}` |

Delivery is at least once. Contract fixtures live in `src/test/resources/fixtures/events`.

## Design
Ports and adapters per feature package `task`: `domain` (records, no framework), `application` (use-case ports and `TaskService`), `web`, `messaging`, `persistence`. Events are written in the same document as the task change and relayed by `OutboxRelay`; see `docs/adr/0001-task-events-outbox-in-task-document.md`.

## Operations
Health: `/actuator/health/readiness` includes MongoDB and RabbitMQ. Relay tuning: `outbox.relay.*`.
