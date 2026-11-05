# Todo web UI

React 19 + TypeScript 7 + SCSS front end for the AddTask and NotificationService services: list tasks, add a task, assign or reassign it, and filter by assignee or unassigned. A notifications panel beside the list polls every 5 seconds, shows all or only unread notifications, and marks them read.

## Run

```bash
npm install
npm run dev        # http://localhost:5173, proxies /api/notifications to localhost:8083 and the rest of /api to AddTask on localhost:8082
```

In Docker the `web` service (see `docker/web/`) serves the build with nginx and proxies `/api/notifications` to `notification:8083` and the rest of `/api/` to `addtask:8082`.

## Check

```bash
npm run typecheck && npm run lint && npm run lint:styles && npm test && npm run build
```

## Layout

- `src/features/tasks` holds the schemas (`task.ts`), API calls, query hooks, components and tests.
- `src/features/notifications` has the same shape for the notifications panel (`notification.ts`, `NotificationsPanel.tsx`).
- `src/shared` holds the fetch client with RFC 9457 problem parsing, the URL search-param hook and generic UI.
- `src/styles` holds design tokens emitted as CSS custom properties, with dark mode via `prefers-color-scheme`.

## API used

`GET /api/tasks[?assignee=]`, `POST /api/tasks`, `PUT /api/tasks/{id}/assignee`, `GET /api/notifications[?unread=true]`, `PUT /api/notifications/{id}/read`. Responses are validated with zod at the boundary; a contract mismatch surfaces as a load error.
