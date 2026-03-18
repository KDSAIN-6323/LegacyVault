import client from './axiosClient';

export interface ShoppingListRef {
  id: string;
  categoryId: string;
  title: string;
  isEncrypted: boolean;
}

export const shoppingListApi = {
  getAll: () => client.get<ShoppingListRef[]>('/shopping-lists'),
};
