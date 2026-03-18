import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from './store';
import { restoreSession } from './store/authSlice';
import LoginPage from './components/auth/LoginPage';
import AppShell from './components/layout/AppShell';

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector((s: RootState) => s.auth.user);
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    dispatch(restoreSession()).finally(() => setInitialized(true));
  }, [dispatch]);

  if (!initialized) return null;
  return user ? <AppShell /> : <LoginPage />;
};

export default App;
