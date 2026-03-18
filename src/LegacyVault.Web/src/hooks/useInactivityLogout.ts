import { useEffect, useRef } from 'react';
import { useDispatch } from 'react-redux';
import type { AppDispatch } from '../store';
import { logoutUser } from '../store/authSlice';
import { keyCache } from '../crypto/keyCache';

const TIMEOUT_MS = 10 * 60 * 1000; // 10 minutes
const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click'];

export function useInactivityLogout(): void {
  const dispatch = useDispatch<AppDispatch>();
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const reset = () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => {
        keyCache.clearAll();
        dispatch(logoutUser());
      }, TIMEOUT_MS);
    };

    reset(); // start timer immediately on mount
    ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, reset, { passive: true }));

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, reset));
    };
  }, [dispatch]);
}
