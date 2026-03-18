import type { Page, ReminderContent, ReminderRecurrence, NotifyUnit } from '../types';

export async function requestNotificationPermission(): Promise<NotificationPermission> {
  if (!('Notification' in window)) return 'denied';
  if (Notification.permission !== 'default') return Notification.permission;
  return Notification.requestPermission();
}

export function notificationsSupported(): boolean {
  return 'Notification' in window;
}

/**
 * Returns the next occurrence date for a reminder (midnight local time).
 * Returns null if the date string is empty.
 */
function getNextOccurrence(
  dateStr: string,
  recurrence: ReminderRecurrence,
  interval: number = 1,
): Date | null {
  if (!dateStr) return null;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const base = new Date(dateStr + 'T00:00:00');
  const n = Math.max(1, interval);

  if (recurrence === 'once') return base;

  let next: Date;

  if (recurrence === 'yearly') {
    const yearDiff = today.getFullYear() - base.getFullYear();
    const cycles = Math.ceil(yearDiff / n);
    next = new Date(base);
    next.setFullYear(base.getFullYear() + cycles * n);
    if (next < today) next.setFullYear(next.getFullYear() + n);
  } else if (recurrence === 'monthly') {
    const monthDiff = (today.getFullYear() - base.getFullYear()) * 12
      + (today.getMonth() - base.getMonth());
    const cycles = Math.ceil(monthDiff / n);
    next = new Date(base);
    next.setMonth(base.getMonth() + cycles * n);
    if (next < today) next.setMonth(next.getMonth() + n);
  } else { // weekly
    const weekDiff = Math.ceil((today.getTime() - base.getTime()) / (7 * 86_400_000));
    const cycles = Math.ceil(weekDiff / n);
    next = new Date(base);
    next.setDate(base.getDate() + cycles * n * 7);
    if (next < today) next.setDate(next.getDate() + n * 7);
  }

  return next;
}

function notifyOffsetMs(before: number, unit: NotifyUnit): number {
  switch (unit) {
    case 'hours': return before * 3_600_000;
    case 'weeks': return before * 7 * 86_400_000;
    default:      return before * 86_400_000; // days
  }
}

/** localStorage key scoped to one event instance — prevents re-firing after reload. */
function firedKey(pageId: string, occurrence: Date): string {
  return `lv_notified_${pageId}_${occurrence.toISOString().slice(0, 10)}`;
}

function daysUntil(target: Date): number {
  const now = new Date();
  return Math.ceil((target.getTime() - now.getTime()) / 86_400_000);
}

function buildBody(occurrence: Date, title: string): string {
  const days = daysUntil(occurrence);
  if (days <= 0) return `${title} is today!`;
  if (days === 1) return `${title} is tomorrow.`;
  return `${title} is in ${days} day${days !== 1 ? 's' : ''}.`;
}

/**
 * Checks all unencrypted Reminder pages and fires a browser notification for
 * any whose trigger window has opened and that haven't already been notified.
 */
export function checkAndFireReminders(pages: Page[]): void {
  if (!notificationsSupported() || Notification.permission !== 'granted') return;

  const now = new Date();

  for (const page of pages) {
    if (page.type !== 'Reminder' || page.isEncrypted) continue;

    let content: ReminderContent;
    try {
      content = JSON.parse(page.content);
    } catch {
      continue;
    }

    if (!content.notifyEnabled || !content.date) continue;

    const occurrence = getNextOccurrence(content.date, content.recurrence, content.recurrenceInterval);
    if (!occurrence) continue;

    // Don't notify for past one-time events
    if (content.recurrence === 'once' && occurrence < now) continue;

    const triggerTime = new Date(occurrence.getTime() - notifyOffsetMs(content.notifyBefore, content.notifyUnit));
    const key = firedKey(page.id, occurrence);

    if (now >= triggerTime && !localStorage.getItem(key)) {
      const TAG_ICONS: Record<string, string> = {
        birthday: '🎂', anniversary: '💍', holiday: '🎉', appointment: '📅', custom: '🔔',
      };
      const icon = TAG_ICONS[content.tag] ?? '🔔';

      new Notification(`${icon} ${page.title}`, {
        body: buildBody(occurrence, page.title),
        tag: page.id,          // deduplicates identical notifications in the OS tray
        icon: '/favicon.ico',
      });

      localStorage.setItem(key, '1');
    }
  }
}
