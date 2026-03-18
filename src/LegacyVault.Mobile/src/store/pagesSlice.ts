import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { pagesApi } from '../api/pagesApi';
import { PageSummary } from '../types';

interface PagesState {
  byCategory: Record<string, PageSummary[]>;
  status: 'idle' | 'loading' | 'failed';
}

export const fetchPages = createAsyncThunk(
  'pages/fetch',
  async (categoryId: string) => {
    const { data } = await pagesApi.getAll(categoryId);
    return { categoryId, pages: data };
  }
);

const pagesSlice = createSlice({
  name: 'pages',
  initialState: { byCategory: {}, status: 'idle' } as PagesState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPages.pending, (s) => { s.status = 'loading'; })
      .addCase(fetchPages.fulfilled, (s, a) => {
        s.status = 'idle';
        s.byCategory[a.payload.categoryId] = a.payload.pages;
      })
      .addCase(fetchPages.rejected, (s) => { s.status = 'failed'; });
  },
});

export default pagesSlice.reducer;
