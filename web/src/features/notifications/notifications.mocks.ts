import { http, HttpResponse } from 'msw';
import type { NotificationWire } from './notification';
import { READ_AT } from './notifications.fixtures';

const NOTIFICATIONS_URL = '/api/notifications';
const READ_URL = '/api/notifications/:id/read';

function problem(body: { status: number }) {
  return HttpResponse.json(body, { status: body.status, headers: { 'Content-Type': 'application/problem+json' } });
}

/** An in-memory NotificationService API: marking a notification read changes what later lists return. */
export function fakeNotificationApi(initial: readonly NotificationWire[]) {
  const notifications: NotificationWire[] = [...initial];
  const unreadQueries: (string | null)[] = [];

  const handlers = [
    http.get(NOTIFICATIONS_URL, ({ request }) => {
      const unread = new URL(request.url).searchParams.get('unread');
      unreadQueries.push(unread);
      return HttpResponse.json(
        unread === 'true' ? notifications.filter((notification) => notification.readAt === null) : notifications,
      );
    }),
    http.put(READ_URL, ({ params }) => {
      const index = notifications.findIndex((notification) => notification.id === params['id']);
      const existing = notifications[index];
      if (existing === undefined) {
        return problem({ status: 404 });
      }
      const updated: NotificationWire = { ...existing, readAt: existing.readAt ?? READ_AT };
      notifications[index] = updated;
      return HttpResponse.json(updated);
    }),
  ];

  return { handlers, unreadQueries };
}

export function failingNotificationRequest(request: 'list' | 'markRead', body: { status: number }) {
  const respond = () => problem(body);
  return request === 'list' ? http.get(NOTIFICATIONS_URL, respond) : http.put(READ_URL, respond);
}
