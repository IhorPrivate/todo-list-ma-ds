import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { renderWithProviders } from '@/test/render';
import { server } from '@/test/server';
import { notificationMessages, type NotificationWire } from './notification';
import { NotificationsPanel } from './NotificationsPanel';
import {
  ALICE,
  aliceAssignedNotification,
  allNotifications,
  broadcastNotification,
  readNotification,
  serverFailure,
} from './notifications.fixtures';
import { failingNotificationRequest, fakeNotificationApi } from './notifications.mocks';

// Accessible names the panel exposes
const PANEL = 'Notifications';
const NOTIFICATION_LIST = 'Notification list';
const MARK_READ_BUTTON = 'Mark as read';
const UNREAD_VIEW = 'Unread';
const RETRY_BUTTON = 'Retry';

// Counts derived from the fixtures
const UNREAD_IN_FIXTURES = 2;
const UNREAD_AFTER_ONE_READ = 1;

// The query parameter value the unread view sends
const UNREAD_QUERY = 'true';

function renderPanel(notifications: readonly NotificationWire[] = allNotifications) {
  const api = fakeNotificationApi(notifications);
  server.use(...api.handlers);
  renderWithProviders(<NotificationsPanel />);
  return api;
}

function notificationItem(notification: NotificationWire) {
  return screen.findByRole('listitem', { name: notification.message });
}

describe('NotificationsPanel', () => {
  it('lists notifications with their recipient', async () => {
    renderPanel();

    const list = await screen.findByRole('list', { name: NOTIFICATION_LIST });

    expect(within(list).getAllByRole('listitem')).toHaveLength(allNotifications.length);
    expect(
      within(await notificationItem(aliceAssignedNotification)).getByText(notificationMessages.forRecipient(ALICE)),
    ).toBeInTheDocument();
    expect(
      within(await notificationItem(broadcastNotification)).getByText(notificationMessages.everyone),
    ).toBeInTheDocument();
  });

  it('shows how many notifications are unread', async () => {
    renderPanel();

    const panel = await screen.findByRole('complementary', { name: PANEL });

    expect(await within(panel).findByRole('status')).toHaveTextContent(
      notificationMessages.unreadCount(UNREAD_IN_FIXTURES),
    );
  });

  it('offers mark as read only on unread notifications', async () => {
    renderPanel();

    const read = await notificationItem(readNotification);

    expect(within(read).queryByRole('button', { name: MARK_READ_BUTTON })).not.toBeInTheDocument();
  });

  it('marks a notification read and updates the unread count', async () => {
    const user = userEvent.setup();
    renderPanel();
    const item = await notificationItem(aliceAssignedNotification);

    await user.click(within(item).getByRole('button', { name: MARK_READ_BUTTON }));

    expect(await screen.findByText(notificationMessages.unreadCount(UNREAD_AFTER_ONE_READ))).toBeInTheDocument();
    expect(within(item).queryByRole('button', { name: MARK_READ_BUTTON })).not.toBeInTheDocument();
  });

  it('asks the server for unread notifications only in the unread view', async () => {
    const user = userEvent.setup();
    const api = renderPanel();
    await notificationItem(readNotification);

    await user.click(screen.getByRole('button', { name: UNREAD_VIEW }));

    const list = await screen.findByRole('list', { name: NOTIFICATION_LIST });
    expect(within(list).getAllByRole('listitem')).toHaveLength(UNREAD_IN_FIXTURES);
    expect(api.unreadQueries).toContain(UNREAD_QUERY);
    expect(screen.getByRole('button', { name: UNREAD_VIEW })).toHaveAttribute('aria-pressed', 'true');
  });

  it('shows the empty state when there are no notifications', async () => {
    renderPanel([]);

    expect(await screen.findByText(notificationMessages.none)).toBeInTheDocument();
  });

  it('shows the caught-up state when nothing is unread', async () => {
    const user = userEvent.setup();
    renderPanel([readNotification]);
    await notificationItem(readNotification);

    await user.click(screen.getByRole('button', { name: UNREAD_VIEW }));

    expect(await screen.findByText(notificationMessages.noneUnread)).toBeInTheDocument();
  });

  it('shows an alert with a retry button when loading fails', async () => {
    server.use(failingNotificationRequest('list', serverFailure));
    renderWithProviders(<NotificationsPanel />);

    const alert = await screen.findByRole('alert');

    expect(alert).toHaveTextContent(notificationMessages.loadFailed);
    expect(within(alert).getByRole('button', { name: RETRY_BUTTON })).toBeInTheDocument();
  });

  it('reports a failed mark as read and keeps the notification unread', async () => {
    const user = userEvent.setup();
    renderPanel();
    server.use(failingNotificationRequest('markRead', serverFailure));
    const item = await notificationItem(aliceAssignedNotification);

    await user.click(within(item).getByRole('button', { name: MARK_READ_BUTTON }));

    expect(await screen.findByRole('alert')).toHaveTextContent(notificationMessages.markReadFailed);
    expect(within(item).getByRole('button', { name: MARK_READ_BUTTON })).toBeEnabled();
  });
});
