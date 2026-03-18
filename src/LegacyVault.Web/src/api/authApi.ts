import axios from 'axios';
import { User } from '../types';

interface AuthResponse {
  accessToken: string;
  user: User;
}

// Use plain axios for auth — no token needed, no circular dependency
const authAxios = axios.create({ baseURL: '/api', withCredentials: true });

export const authApi = {
  register: (data: { username: string; email: string; password: string }) =>
    authAxios.post<AuthResponse>('/auth/register', data),

  login: (data: { username: string; password: string }) =>
    authAxios.post<AuthResponse>('/auth/login', data),

  refresh: () =>
    authAxios.post<AuthResponse>('/auth/refresh'),

  logout: () =>
    authAxios.post('/auth/logout'),

  health: () =>
    authAxios.get('/auth/health'),

  resetPassword: (data: { username: string; email: string; newPassword: string }) =>
    authAxios.post('/auth/reset-password', data),
};
