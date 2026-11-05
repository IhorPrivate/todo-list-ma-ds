import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { NotificationView } from './notification';
import { fetchNotifications, markNotificationRead } from './notifications.api';

/** Notifications are created asynchronously from task events, so the panel polls for new ones. */
export const NOTIFICATIONS_POLL_MS = 5000;

export const notificationKeys = {
  all: ['notifications'] as const,
  list: (view: NotificationView) => [...notificationKeys.all, 'list', view] as const,
};

export function useNotifications(view: NotificationView) {
  return useQuery({
    queryKey: notificationKeys.list(view),
    queryFn: () => fetchNotifications(view),
    refetchInterval: NOTIFICATIONS_POLL_MS,
  });
}

export function useMarkNotificationRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: notificationKeys.all }),
  });
}
