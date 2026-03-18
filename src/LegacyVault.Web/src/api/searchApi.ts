import client from './axiosClient';

export interface SearchResult {
  pageId: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  type: string;
  title: string;
  isEncrypted: boolean;
  updatedAt: string;
}

export const searchApi = {
  search: (q: string) => client.get<SearchResult[]>('/search', { params: { q } }),
};
