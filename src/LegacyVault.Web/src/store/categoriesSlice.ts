import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { categoriesApi } from '../api/categoriesApi';
import type { Category } from '../types';

interface CategoriesState {
  items: Category[];
  selectedId: string | null;
  status: 'idle' | 'loading' | 'failed';
  unlockedCategoryIds: string[];
}

const initialState: CategoriesState = {
  items: [],
  selectedId: null,
  status: 'idle',
  unlockedCategoryIds: [],
};

export const fetchCategories = createAsyncThunk('categories/fetchAll', async () => {
  const { data } = await categoriesApi.getAll();
  return data;
});

export const createCategory = createAsyncThunk(
  'categories/create',
  async (payload: { name: string; icon: string; type: 'General' | 'Vault'; isEncrypted: boolean; encryptionSalt?: string; passwordHint?: string }) => {
    const { data } = await categoriesApi.create(payload);
    return data;
  }
);

export const deleteCategory = createAsyncThunk('categories/delete', async (id: string) => {
  await categoriesApi.delete(id);
  return id;
});

export const toggleCategoryFavorite = createAsyncThunk(
  'categories/toggleFavorite',
  async (id: string) => {
    const { data } = await categoriesApi.toggleFavorite(id);
    return { id, isFavorite: data.isFavorite };
  }
);

export const renameCategory = createAsyncThunk(
  'categories/rename',
  async ({ id, name }: { id: string; name: string }) => {
    await categoriesApi.update(id, { name });
    return { id, name };
  }
);

const categoriesSlice = createSlice({
  name: 'categories',
  initialState,
  reducers: {
    selectCategory: (state, action: PayloadAction<string | null>) => {
      state.selectedId = action.payload;
    },
    unlockCategory: (state, action: PayloadAction<string>) => {
      if (!state.unlockedCategoryIds.includes(action.payload))
        state.unlockedCategoryIds.push(action.payload);
    },
    lockCategory: (state, action: PayloadAction<string>) => {
      state.unlockedCategoryIds = state.unlockedCategoryIds.filter((id) => id !== action.payload);
    },
    updateCategoryLocally: (state, action: PayloadAction<Partial<Category> & { id: string }>) => {
      const idx = state.items.findIndex((c) => c.id === action.payload.id);
      if (idx !== -1) state.items[idx] = { ...state.items[idx], ...action.payload };
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCategories.pending, (state) => { state.status = 'loading'; })
      .addCase(fetchCategories.fulfilled, (state, action) => {
        state.status = 'idle';
        state.items = action.payload;
      })
      .addCase(fetchCategories.rejected, (state) => { state.status = 'failed'; })
      .addCase(createCategory.fulfilled, (state, action) => {
        state.items.push(action.payload);
        state.selectedId = action.payload.id;
      })
      .addCase(deleteCategory.fulfilled, (state, action) => {
        state.items = state.items.filter((c) => c.id !== action.payload);
        if (state.selectedId === action.payload) state.selectedId = null;
        state.unlockedCategoryIds = state.unlockedCategoryIds.filter((id) => id !== action.payload);
      })
      .addCase(renameCategory.fulfilled, (state, action) => {
        const idx = state.items.findIndex((c) => c.id === action.payload.id);
        if (idx !== -1) state.items[idx].name = action.payload.name;
      })
      .addCase(toggleCategoryFavorite.fulfilled, (state, action) => {
        const idx = state.items.findIndex((c) => c.id === action.payload.id);
        if (idx !== -1) state.items[idx].isFavorite = action.payload.isFavorite;
      });
  },
});

export const { selectCategory, unlockCategory, lockCategory, updateCategoryLocally } = categoriesSlice.actions;
export default categoriesSlice.reducer;
