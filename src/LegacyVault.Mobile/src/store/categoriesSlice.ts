import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { apiClient } from '../api/client';
import { Category } from '../types';

interface CategoriesState {
  items: Category[];
  selectedId: string | null;
  status: 'idle' | 'loading' | 'failed';
}

export const fetchCategories = createAsyncThunk('categories/fetchAll', async () => {
  const { data } = await apiClient.get<Category[]>('/api/categories');
  return data;
});

export const createCategory = createAsyncThunk(
  'categories/create',
  async (payload: { name: string; icon: string; isEncrypted: boolean; encryptionSalt?: string }) => {
    const { data } = await apiClient.post<Category>('/api/categories', payload);
    return data;
  }
);

const categoriesSlice = createSlice({
  name: 'categories',
  initialState: { items: [], selectedId: null, status: 'idle' } as CategoriesState,
  reducers: {
    selectCategory: (state, action: PayloadAction<string>) => { state.selectedId = action.payload; },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCategories.pending, (s) => { s.status = 'loading'; })
      .addCase(fetchCategories.fulfilled, (s, a) => { s.status = 'idle'; s.items = a.payload; })
      .addCase(fetchCategories.rejected, (s) => { s.status = 'failed'; })
      .addCase(createCategory.fulfilled, (s, a) => { s.items.push(a.payload); });
  },
});

export const { selectCategory } = categoriesSlice.actions;
export default categoriesSlice.reducer;
