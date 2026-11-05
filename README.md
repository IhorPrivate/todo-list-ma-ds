# Todo list on RabbitMQ

A small task tracker built from independent services that talk through RabbitMQ:

| Part | Folder | Port | What it does |
|---|---|---|---|
| Web UI | `web/` | 8080 (Docker), 5173 (dev) | React app: list, add and assign tasks; notifications panel |
| AddTask | `AddTask/` | 8082 | Tasks REST API; consumes add/assign commands; publishes task events |
| NotificationService | `NotificationService/` | 8083 | Turns task events into notifications; REST API to read them |
| todo-list | `src/` (root project) | 8081 | Spring Boot app connected to the broker |
| RabbitMQ | `docker/rabbitmq/` | 5672, management UI 15672 | Message broker |
| MongoDB | `docker/mongo-db/` | 27017 | One database per service: `tasks`, `notifications` |

Each module has its own README with its API and message contracts. Architecture decisions live in `docs/adr/`.

## Prerequisites

- Docker with Compose v2, and the daemon running (`sudo systemctl start docker` on Linux).
- JDK 25 and Maven 3.9+ for building and running the Java services outside Docker. Every pom uses
  `maven-toolchains-plugin`, so `~/.m2/toolchains.xml` must declare a JDK 25:

  ```xml
  <toolchains>
    <toolchain>
      <type>jdk</type>
      <provides><version>25</version></provides>
      <configuration><jdkHome>/path/to/jdk-25</jdkHome></configuration>
    </toolchain>
  </toolchains>
  ```

- Node.js 20.19+ (22 LTS recommended) for working on the web UI outside Docker.

## Run everything in Docker

```bash
mvn package                         # builds the root app into docker/todo-list.jar
cd docker
cp .env.example .env                # optional: change credentials
docker compose up --build
```

Docker builds AddTask, NotificationService and the web UI itself; only the root app's jar is built on the host.

Then open:

- Web UI: http://localhost:8080
- RabbitMQ management: http://localhost:15672 (user `todo`, password `todo` unless changed in `.env`)
- AddTask Swagger UI: http://localhost:8082/swagger-ui.html

Stop with `docker compose down`; add `-v` to also delete the broker and database volumes.

MongoDB creates the per-service users only when its volume is first initialised. If you change the database
credentials in `.env` after the first start, run `docker compose down -v` once so they are created again.

## Run locally for development

Start only the broker and database in Docker, then run the services from source:

```bash
docker compose -f docker/compose.infra.yaml up -d

cd AddTask && mvn spring-boot:run                  # terminal 1, port 8082
cd NotificationService && mvn spring-boot:run      # terminal 2, port 8083
cd web && npm install && npm run dev               # terminal 3, http://localhost:5173
```

The Vite dev server proxies `/api/notifications` to port 8083 and the rest of `/api` to port 8082.

The root app starts the infrastructure itself through Spring Boot's Docker Compose support:

```bash
./mvnw spring-boot:run                             # port 8081, uses docker/compose.infra.yaml
```

## Try it

1. Open the web UI and add a task with an assignee.
2. Within a few seconds the notifications panel shows the notification created from that task's event.
3. Assign the task to someone else: the new assignee is notified, and the previous assignee is told it was reassigned.

Tasks can also be added by publishing a command to the `tasks.commands` exchange; see `AddTask/README.md` for the
message format.

## Test

```bash
mvn test                                           # in the root, AddTask/ and NotificationService/
cd web && npm run typecheck && npm run lint && npm run lint:styles && npm test
```

The Java integration tests use Testcontainers and are skipped when the Docker daemon is not running.

## Repository layout

```
AddTask/               tasks service
NotificationService/   notifications service
web/                   React UI
src/                   root todo-list Spring Boot app
docker/                Dockerfiles, compose files, broker and database config
docs/adr/              architecture decision records
```
