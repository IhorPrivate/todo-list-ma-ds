import { z } from 'zod';

declare const brand: unique symbol;
type Brand<T, Name extends string> = T & { readonly [brand]: Name };

export type NotificationId = Brand<string, 'NotificationId'>;

function notificationId(value: string): NotificationId {
  // oxlint-disable-next-line typescript/no-unsafe-type-assertion -- the one sanctioned brand cast
  return value as NotificationId;
}

export const notificationMessages = {
  loadFailed: 'Could not load notifications.',
  markReadFailed: 'Could not mark the notification as read. Try again.',
  none: 'No notifications yet.',
  noneUnread: 'You are all caught up.',
  everyone: 'Everyone',
  forRecipient: (recipient: string) => `For ${recipient}`,
  unreadCount: (count: number) => `${count} unread`,
} as const;

const instant = z.iso.datetime({ offset: true }).transform((value) => new Date(value));

export const NotificationSchema = z.object({
  id: z.uuid().transform(notificationId),
  type: z.enum(['TASK_ADDED', 'TASK_ASSIGNED', 'TASK_UNASSIGNED']),
  recipient: z.string().nullable(),
  message: z.string(),
  taskId: z.uuid(),
  createdAt: instant,
  readAt: instant.nullable(),
});

export const NotificationListSchema = z.array(NotificationSchema).readonly();

export type Notification = z.output<typeof NotificationSchema>;
export type NotificationWire = z.input<typeof NotificationSchema>;

/** Which notifications the panel shows; the server filters, so this maps onto the `unread` query parameter. */
export type NotificationView = 'all' | 'unread';

export function isUnread(notification: Notification): boolean {
  return notification.readAt === null;
}
