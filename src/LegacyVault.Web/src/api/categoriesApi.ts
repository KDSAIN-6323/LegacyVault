import client from './axiosClient';
import { Category } from '../types';

export const categoriesApi = {
  getAll: () => client.get<Category[]>('/categories'),

  getById: (id: string) => client.get<Category>(`/categories/${id}`),

  create: (data: { name: string; icon: string; isEncrypted: boolean; encryptionSalt?: string; passwordHint?: string }) =>
    client.post<Category>('/categories', data),

  update: (id: string, data: { name?: string; icon?: string }) =>
    client.put(`/categories/${id}`, data),

  delete: (id: string) => client.delete(`/categories/${id}`),

  toggleFavorite: (id: string) =>
    client.patch<{ isFavorite: boolean }>(`/categories/${id}/favorite`),
};
