import { useId, useState } from 'react';
import { Button } from '@/shared/ui/Button';
import { NotificationItem } from './NotificationItem';
import styles from './NotificationsPanel.module.scss';
import { isUnread, notificationMessages, type NotificationView } from './notification';
import { useMarkNotificationRead, useNotifications } from './useNotifications';

const VIEWS = [
  { view: 'all', label: 'All' },
  { view: 'unread', label: 'Unread' },
] as const satisfies readonly { view: NotificationView; label: string }[];

export function NotificationsPanel() {
  const headingId = useId();
  const [view, setView] = useState<NotificationView>('all');

  return (
    <aside className={styles.panel} aria-labelledby={headingId}>
      <header className={styles.header}>
        <h2 id={headingId} className={styles.title}>
          Notifications
        </h2>
        <div className={styles.views} role="group" aria-label="Show notifications">
          {VIEWS.map((option) => (
            <Button
              key={option.view}
              variant="secondary"
              aria-pressed={view === option.view}
              onClick={() => setView(option.view)}
            >
              {option.label}
            </Button>
          ))}
        </div>
      </header>
      <NotificationListContent view={view} />
    </aside>
  );
}

function NotificationListContent({ view }: { view: NotificationView }) {
  const notifications = useNotifications(view);
  const markRead = useMarkNotificationRead();

  if (notifications.isPending) {
    return (
      <p role="status" className={styles.state}>
        Loading notifications…
      </p>
    );
  }
  if (notifications.isError) {
    return (
      <div role="alert" className={styles.alert}>
        <span>{notificationMessages.loadFailed}</span>
        <Button variant="secondary" onClick={() => void notifications.refetch()}>
          Retry
        </Button>
      </div>
    );
  }
  if (notifications.data.length === 0) {
    return <p className={styles.state}>{view === 'all' ? notificationMessages.none : notificationMessages.noneUnread}</p>;
  }
  return (
    <>
      <p role="status" className={styles.count}>
        {notificationMessages.unreadCount(notifications.data.filter(isUnread).length)}
      </p>
      {markRead.isError && (
        <p role="alert" className={styles.error}>
          {notificationMessages.markReadFailed}
        </p>
      )}
      <ul role="list" aria-label="Notification list" className={styles.list}>
        {notifications.data.map((notification) => (
          <NotificationItem
            key={notification.id}
            notification={notification}
            marking={markRead.isPending && markRead.variables === notification.id}
            onMarkRead={(target) => markRead.mutate(target.id)}
          />
        ))}
      </ul>
    </>
  );
}
