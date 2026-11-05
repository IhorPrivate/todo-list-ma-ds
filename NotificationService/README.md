# NotificationService

Owns: notifications generated from task activity, and their read state. MongoDB database `notifications` is the
system of record.

The AddTask service stores tasks and then publishes events. This service consumes them, decides who to notify,
stores each notification once, outputs it through a delivery channel, and serves it over REST.

## Runs on

Java 25, Spring Boot 4.1. Port `8083`.

```bash
mvn spring-boot:run        # needs RabbitMQ on localhost:5672 and MongoDB on localhost:27017
mvn test                   # unit and slice tests; Testcontainers tests run when Docker is up
```

`~/.mavenrc` selects JDK 21 under this path, so the pom uses `maven-toolchains-plugin` to compile and test with
the JDK 25 registered in `~/.m2/toolchains.xml`.

Connection settings: `spring.mongodb.uri` (default
`mongodb://notification:notification@localhost:27017/notifications?authSource=notifications`) and the standard
`spring.rabbitmq.*` properties, or their `SPRING_MONGODB_URI` / `SPRING_RABBITMQ_*` environment forms.

## Contracts consumed

Exchange `tasks.events` (topic, owned by AddTask), JSON bodies.

| Routing key | Event | Queue | Dead-letter queue |
|---|---|---|---|
| `task.added` | `TaskAdded { eventId, occurredAt, taskId, title, description?, assignee? }` | `notifications.task-added` | `notifications.task-added.dlq` |
| `task.assigned` | `TaskAssigned { eventId, occurredAt, taskId, title, assignee, previousAssignee? }` | `notifications.task-assigned` | `notifications.task-assigned.dlq` |

Both queues are quorum queues dead-lettering through `notifications.dead-letters`. This service declares all of
it on startup, including `tasks.events`, so start order does not matter.

Failures are retried three times with backoff. Two kinds of poison message skip retries and go straight to the
DLQ: unparseable bodies, and events missing a required field.

## Notification rules

| Event | Notifications |
|---|---|
| `TaskAdded` with assignee | `TASK_ADDED` to the assignee |
| `TaskAdded` without assignee | `TASK_ADDED` broadcast to everyone |
| `TaskAssigned` without previous assignee | `TASK_ASSIGNED` to the assignee |
| `TaskAssigned` with a different previous assignee | `TASK_ASSIGNED` to the new one, `TASK_UNASSIGNED` to the previous one |

Each notification id is a name-based UUID of the event id, type and recipient. A redelivered event therefore
yields the same ids, the database rejects the duplicates, and nothing is stored or output twice. Message text
comes from `src/main/resources/messages.properties`.

Output goes through the `NotificationChannel` port. The current adapter writes one INFO log line per
notification. E-mail or websocket channels implement the same port. Delivery happens once, right after the first
insert; the stored notification served over REST is the source of truth.

## REST

Base path `/api/notifications`. Errors are RFC 9457 problem details.

| Method and path | Result |
|---|---|
| `GET /api/notifications?recipient=&unread=` | `200`, newest first. `recipient` includes broadcasts; `unread=true` or `false` filters by read state |
| `GET /api/notifications/{id}` | `200`, or `404` |
| `PUT /api/notifications/{id}/read` | `200` with the notification; idempotent, keeps the first `readAt`; `404` when unknown |

Response fields: `id`, `type`, `recipient` (`null` for broadcasts), `message`, `taskId`, `createdAt`, `readAt`
(`null` while unread).

## Layout

`notification/domain` holds the framework-free model and policy. `notification/application` holds ports and
services. Adapters live in `web`, `messaging`, `persistence`, `delivery` and `text`.
