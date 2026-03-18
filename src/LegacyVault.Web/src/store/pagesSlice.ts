import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import type { PayloadAction } from '@reduxjs/toolkit';
import { pagesApi } from '../api/pagesApi';
import type { Page, PageType } from '../types';


interface PagesState {
  items: Page[];
  selectedId: string | null;
  pendingSelectId: string | null;  // set by search navigation; consumed on fetchPages.fulfilled
  status: 'idle' | 'loading' | 'failed';
  currentCategoryId: string | null;
}

const initialState: PagesState = {
  items: [],
  selectedId: null,
  pendingSelectId: null,
  status: 'idle',
  currentCategoryId: null,
};

export const fetchPages = createAsyncThunk('pages/fetchAll', async (categoryId: string) => {
  const { data } = await pagesApi.getAll(categoryId);
  return { categoryId, pages: data };
});

export const createPage = createAsyncThunk(
  'pages/create',
  async (payload: {
    categoryId: string;
    title: string;
    type: PageType;
    content: string;
    isEncrypted: boolean;
    encryptionSalt?: string;
    encryptionIV?: string;
  }) => {
    const { categoryId, ...rest } = payload;
    const { data } = await pagesApi.create(categoryId, rest);
    return data;
  }
);

export const savePage = createAsyncThunk(
  'pages/save',
  async (payload: {
    categoryId: string;
    id: string;
    title?: string;
    content?: string;
    encryptionIV?: string;
  }) => {
    const { categoryId, id, ...rest } = payload;
    await pagesApi.update(categoryId, id, rest);
    return payload;
  }
);

export const movePage = createAsyncThunk(
  'pages/move',
  async ({ categoryId, id, targetCategoryId }: { categoryId: string; id: string; targetCategoryId: string }) => {
    await pagesApi.move(categoryId, id, targetCategoryId);
    return id;
  }
);

export const togglePageFavorite = createAsyncThunk(
  'pages/toggleFavorite',
  async ({ categoryId, id }: { categoryId: string; id: string }) => {
    const { data } = await pagesApi.toggleFavorite(categoryId, id);
    return { id, isFavorite: data.isFavorite };
  }
);

export const deletePage = createAsyncThunk(
  'pages/delete',
  async ({ categoryId, id }: { categoryId: string; id: string }) => {
    await pagesApi.delete(categoryId, id);
    return id;
  }
);

const pagesSlice = createSlice({
  name: 'pages',
  initialState,
  reducers: {
    selectPage: (state, action: PayloadAction<string | null>) => {
      state.selectedId = action.payload;
    },
    setPendingSelect: (state, action: PayloadAction<string | null>) => {
      state.pendingSelectId = action.payload;
    },
    clearPages: (state) => {
      state.items = [];
      state.selectedId = null;
      state.pendingSelectId = null;
      state.currentCategoryId = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPages.pending, (state) => { state.status = 'loading'; })
      .addCase(fetchPages.fulfilled, (state, action) => {
        state.status = 'idle';
        state.items = action.payload.pages;
        state.currentCategoryId = action.payload.categoryId;
        // Consume a pending search navigation target
        if (state.pendingSelectId) {
          state.selectedId = state.pendingSelectId;
          state.pendingSelectId = null;
        }
      })
      .addCase(fetchPages.rejected, (state) => { state.status = 'failed'; })
      .addCase(createPage.fulfilled, (state, action) => {
        state.items.push(action.payload);
        state.selectedId = action.payload.id;
      })
      .addCase(savePage.fulfilled, (state, action) => {
        const idx = state.items.findIndex((p) => p.id === action.payload.id);
        if (idx !== -1) {
          if (action.payload.title) state.items[idx].title = action.payload.title;
          if (action.payload.content !== undefined) state.items[idx].content = action.payload.content;
        }
      })
      .addCase(movePage.fulfilled, (state, action) => {
        state.items = state.items.filter((p) => p.id !== action.payload);
        if (state.selectedId === action.payload) state.selectedId = null;
      })
      .addCase(deletePage.fulfilled, (state, action) => {
        state.items = state.items.filter((p) => p.id !== action.payload);
        if (state.selectedId === action.payload) state.selectedId = null;
      })
      .addCase(togglePageFavorite.fulfilled, (state, action) => {
        const idx = state.items.findIndex((p) => p.id === action.payload.id);
        if (idx !== -1) state.items[idx].isFavorite = action.payload.isFavorite;
      });
  },
});

export const { selectPage, setPendingSelect, clearPages } = pagesSlice.actions;
export default pagesSlice.reducer;
