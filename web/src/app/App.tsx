import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';
import { NotificationsPanel } from '@/features/notifications/NotificationsPanel';
import { TasksPage } from '@/features/tasks/TasksPage';
import styles from './App.module.scss';

export function App() {
  const [queryClient] = useState(() => new QueryClient());
  return (
    <QueryClientProvider client={queryClient}>
      <div className={styles.layout}>
        <TasksPage />
        <div className={styles.side}>
          <NotificationsPanel />
        </div>
      </div>
    </QueryClientProvider>
  );
}
