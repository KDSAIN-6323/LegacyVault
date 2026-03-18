import { useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';
import type { RootState } from '../store';
import { checkAndFireReminders } from '../services/notificationService';

const CHECK_INTERVAL_MS = 60_000; // re-check every minute

/**
 * Fires browser notifications for due reminders.
 * Call once at the top of AppShell — no props needed.
 */
export function useReminderNotifications(): void {
  const pages = useSelector((s: RootState) => s.pages.items);
  // Keep a ref so the interval closure always sees the latest pages
  // without needing to teardown/recreate the interval on every update.
  const pagesRef = useRef(pages);

  useEffect(() => { pagesRef.current = pages; }, [pages]);

  useEffect(() => {
    checkAndFireReminders(pagesRef.current);
    const id = setInterval(() => checkAndFireReminders(pagesRef.current), CHECK_INTERVAL_MS);
    return () => clearInterval(id);
  }, []); // intentionally empty — interval is created once
}
