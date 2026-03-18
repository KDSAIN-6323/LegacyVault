import client from './axiosClient';
import { Page, PageType } from '../types';

export const pagesApi = {
  getAll: (categoryId: string) =>
    client.get<Page[]>(`/categories/${categoryId}/pages`),

  getById: (categoryId: string, id: string) =>
    client.get<Page>(`/categories/${categoryId}/pages/${id}`),

  create: (categoryId: string, data: {
    title: string;
    type: PageType;
    content: string;
    isEncrypted: boolean;
    encryptionSalt?: string;
    encryptionIV?: string;
  }) => client.post<Page>(`/categories/${categoryId}/pages`, data),

  update: (categoryId: string, id: string, data: {
    title?: string;
    content?: string;
    encryptionIV?: string;
    sortOrder?: number;
  }) => client.put(`/categories/${categoryId}/pages/${id}`, data),

  move: (categoryId: string, id: string, targetCategoryId: string) =>
    client.patch(`/categories/${categoryId}/pages/${id}/move`, { targetCategoryId }),

  delete: (categoryId: string, id: string) =>
    client.delete(`/categories/${categoryId}/pages/${id}`),

  toggleFavorite: (categoryId: string, id: string) =>
    client.patch<{ isFavorite: boolean }>(`/categories/${categoryId}/pages/${id}/favorite`),
};
