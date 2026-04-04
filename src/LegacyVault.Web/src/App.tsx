import React, { useCallback, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from './store';
import { restoreSession } from './store/authSlice';
import LoginPage from './components/auth/LoginPage';
import AppShell from './components/layout/AppShell';
import AdventureGame from './components/easter-egg/AdventureGame';
import { useKonamiCode } from './hooks/useKonamiCode';

const App: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector((s: RootState) => s.auth.user);
  const [initialized, setInitialized] = useState(false);
  const [gameOpen, setGameOpen] = useState(false);

  useEffect(() => {
    dispatch(restoreSession()).finally(() => setInitialized(true));
  }, [dispatch]);

  const openGame = useCallback(() => setGameOpen(true), []);
  const closeGame = useCallback(() => setGameOpen(false), []);
  useKonamiCode(openGame);

  if (!initialized) return null;
  return (
    <>
      {user ? <AppShell /> : <LoginPage />}
      {gameOpen && <AdventureGame onClose={closeGame} />}
    </>
  );
};

export default App;
