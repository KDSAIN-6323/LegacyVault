import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:5000',
      '/uploads': 'http://localhost:5000',
    },
  },
  // Capacitor requires absolute asset paths when bundled in the APK
  base: process.env.VITE_ANDROID_BUILD ? './' : '/',
});
