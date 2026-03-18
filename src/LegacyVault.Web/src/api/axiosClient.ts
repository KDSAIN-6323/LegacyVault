import axios from 'axios';
import { store } from '../store';
import { logout, setAccessToken } from '../store/authSlice';

const API_BASE: string = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api';

const client = axios.create({
  baseURL: API_BASE,
  withCredentials: true,  // Required for httpOnly refresh token cookie
});

// Attach access token to every request
client.interceptors.request.use((config) => {
  const token = store.getState().auth.accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// On 401: attempt silent token refresh, then retry original request
let isRefreshing = false;
let pendingQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = [];

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return client(originalRequest);
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const { data } = await axios.post(`${API_BASE}/auth/refresh`, {}, { withCredentials: true, timeout: 8000 });
      const newToken: string = data.accessToken;
      store.dispatch(setAccessToken(newToken));
      pendingQueue.forEach((p) => p.resolve(newToken));
      pendingQueue = [];
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return client(originalRequest);
    } catch {
      pendingQueue.forEach((p) => p.reject(new Error('Session expired')));
      pendingQueue = [];
      store.dispatch(logout());
      return Promise.reject(error);
    } finally {
      isRefreshing = false;
    }
  }
);

export default client;
