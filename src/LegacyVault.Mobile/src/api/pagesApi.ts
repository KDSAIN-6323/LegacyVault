import { apiClient } from './client';
import { Page, PageSummary } from '../types';

export const pagesApi = {
  getAll: (categoryId: string) =>
    apiClient.get<PageSummary[]>(`/api/categories/${categoryId}/pages`),
  getOne: (categoryId: string, pageId: string) =>
    apiClient.get<Page>(`/api/categories/${categoryId}/pages/${pageId}`),
};
