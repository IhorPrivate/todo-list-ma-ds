import { useId } from 'react';
import { Button } from '@/shared/ui/Button';
import styles from './NotificationItem.module.scss';
import { isUnread, notificationMessages, type Notification } from './notification';

const createdFormat = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });

type NotificationItemProps = {
  notification: Notification;
  onMarkRead: (notification: Notification) => void;
  marking: boolean;
};

export function NotificationItem({ notification, onMarkRead, marking }: NotificationItemProps) {
  const messageId = useId();
  const unread = isUnread(notification);
  return (
    <li className={styles.item} aria-labelledby={messageId} data-unread={unread}>
      <p id={messageId} className={styles.message}>
        {notification.message}
      </p>
      <p className={styles.meta}>
        <span>
          {notification.recipient === null
            ? notificationMessages.everyone
            : notificationMessages.forRecipient(notification.recipient)}
        </span>
        <time dateTime={notification.createdAt.toISOString()}>{createdFormat.format(notification.createdAt)}</time>
      </p>
      {unread && (
        <Button
          variant="secondary"
          className={styles.action}
          disabled={marking}
          aria-describedby={messageId}
          onClick={() => onMarkRead(notification)}
        >
          Mark as read
        </Button>
      )}
    </li>
  );
}
