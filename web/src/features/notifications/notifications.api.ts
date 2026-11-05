import { requestJson } from '@/shared/lib/apiClient';
import {
  NotificationListSchema,
  NotificationSchema,
  type Notification,
  type NotificationId,
  type NotificationView,
} from './notification';

const NOTIFICATIONS_PATH = '/api/notifications';

export function fetchNotifications(view: NotificationView): Promise<readonly Notification[]> {
  return requestJson(NOTIFICATIONS_PATH, NotificationListSchema, {
    query: { unread: view === 'unread' ? 'true' : undefined },
  });
}

export function markNotificationRead(id: NotificationId): Promise<Notification> {
  return requestJson(`${NOTIFICATIONS_PATH}/${encodeURIComponent(id)}/read`, NotificationSchema, { method: 'PUT' });
}
