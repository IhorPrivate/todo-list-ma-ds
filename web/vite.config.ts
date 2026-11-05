/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vite';

const ADD_TASK_SERVICE = 'http://localhost:8082';
const NOTIFICATION_SERVICE = 'http://localhost:8083';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    // Vite matches proxy keys in insertion order, so the more specific path comes first.
    proxy: { '/api/notifications': NOTIFICATION_SERVICE, '/api': ADD_TASK_SERVICE },
  },
  css: {
    preprocessorOptions: {
      scss: {
        fatalDeprecations: ['import', 'global-builtin', 'slash-div'],
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: false,
    setupFiles: ['./src/test/setup.ts'],
    css: { modules: { classNameStrategy: 'non-scoped' } },
  },
});
