import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { User } from '../types';

interface AuthState {
  user: User | null;
}

const authSlice = createSlice({
  name: 'auth',
  initialState: { user: null } as AuthState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => { state.user = action.payload; },
    clearUser: (state) => { state.user = null; },
  },
});

export const { setUser, clearUser } = authSlice.actions;
export default authSlice.reducer;
