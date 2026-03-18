import React, { useEffect, useState, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { selectCategory } from '../../store/categoriesSlice';
import { setPendingSelect } from '../../store/pagesSlice';
import { remindersApi } from '../../api/remindersApi';
import { pagesApi } from '../../api/pagesApi';
import type { ReminderPage } from '../../api/remindersApi';
import type { ReminderContent, ReminderRecurrence, ReminderTag } from '../../types';
import './CalendarView.css';

type CalendarMode = 'week' | 'month';

interface ParsedReminder {
  pageId: string;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  title: string;
  date: string;
  endDate?: string;
  tag: ReminderTag;
  recurrence: ReminderRecurrence;
  recurrenceInterval: number;
}

const TAG_COLORS: Record<string, string> = {
  birthday:    'var(--cal-tag-birthday)',
  anniversary: 'var(--cal-tag-anniversary)',
  holiday:     'var(--cal-tag-holiday)',
  appointment: 'var(--cal-tag-appointment)',
  custom:      'var(--cal-tag-custom)',
};

const TAG_ICONS: Record<string, string> = {
  birthday: '🎂', anniversary: '💍', holiday: '🎉',
  appointment: '📅', custom: '🔔',
};

const TAG_OPTIONS: { value: ReminderTag; label: string }[] = [
  { value: 'custom',      label: '🔔 Custom' },
  { value: 'birthday',    label: '🎂 Birthday' },
  { value: 'anniversary', label: '💍 Anniversary' },
  { value: 'appointment', label: '📅 Appointment' },
  { value: 'holiday',     label: '🎉 Holiday' },
];

const DOW = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function toDateKey(d: Date): string {
  return d.toISOString().slice(0, 10);
}

// Returns the nth weekday of a month (n=1 = first, n=-1 = last)
function nthWeekday(year: number, month: number, weekday: number, n: number): Date {
  if (n > 0) {
    const first = new Date(year, month, 1);
    const diff = (weekday - first.getDay() + 7) % 7;
    return new Date(year, month, 1 + diff + (n - 1) * 7);
  }
  const last = new Date(year, month + 1, 0);
  const diff = (last.getDay() - weekday + 7) % 7;
  return new Date(year, month, last.getDate() - diff);
}

function getUSHolidays(year: number): Map<string, string> {
  const h = new Map<string, string>();
  const add = (d: Date, name: string) => h.set(toDateKey(d), name);

  add(new Date(year,  0,  1), "🎆 New Year's Day");
  add(new Date(year,  5, 19), "✊ Juneteenth");
  add(new Date(year,  6,  4), "🇺🇸 Independence Day");
  add(new Date(year, 10, 11), "🎖 Veterans Day");
  add(new Date(year, 11, 25), "🎄 Christmas Day");

  add(nthWeekday(year,  0, 1,  3), "✊ MLK Day");
  add(nthWeekday(year,  1, 1,  3), "🏛 Presidents' Day");
  add(nthWeekday(year,  4, 1, -1), "🪖 Memorial Day");
  add(nthWeekday(year,  8, 1,  1), "👷 Labor Day");
  add(nthWeekday(year,  9, 1,  2), "🌎 Columbus Day");
  add(nthWeekday(year, 10, 4,  4), "🦃 Thanksgiving");

  return h;
}

function getMonthGrid(year: number, month: number): (Date | null)[][] {
  const first = new Date(year, month, 1);
  const last = new Date(year, month + 1, 0);
  const rows: (Date | null)[][] = [];
  let row: (Date | null)[] = Array(first.getDay()).fill(null);
  for (let d = 1; d <= last.getDate(); d++) {
    row.push(new Date(year, month, d));
    if (row.length === 7) { rows.push(row); row = []; }
  }
  if (row.length > 0) {
    while (row.length < 7) row.push(null);
    rows.push(row);
  }
  return rows;
}

function getWeekDays(anchor: Date): Date[] {
  const start = new Date(anchor);
  start.setDate(anchor.getDate() - anchor.getDay());
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    return d;
  });
}

function getOccurrencesInRange(r: ParsedReminder, days: Date[]): Set<string> {
  if (!r.date) return new Set();
  const base = new Date(r.date + 'T00:00:00');
  const n = Math.max(1, r.recurrenceInterval ?? 1);
  const keys = new Set<string>();

  for (const day of days) {
    const dayKey = toDateKey(day);

    if (r.recurrence === 'once') {
      if (r.endDate) {
        if (dayKey >= r.date && dayKey <= r.endDate) keys.add(dayKey);
      } else {
        if (dayKey === r.date) keys.add(dayKey);
      }
    } else if (r.recurrence === 'yearly') {
      if (day.getMonth() === base.getMonth() && day.getDate() === base.getDate()) {
        const yearDiff = day.getFullYear() - base.getFullYear();
        if (yearDiff >= 0 && yearDiff % n === 0) keys.add(dayKey);
      }
    } else if (r.recurrence === 'monthly') {
      if (day.getDate() === base.getDate()) {
        const monthDiff = (day.getFullYear() - base.getFullYear()) * 12
          + (day.getMonth() - base.getMonth());
        if (monthDiff >= 0 && monthDiff % n === 0) keys.add(dayKey);
      }
    } else if (r.recurrence === 'weekly') {
      if (day.getDay() === base.getDay()) {
        const weekDiff = Math.round((day.getTime() - base.getTime()) / (7 * 86_400_000));
        if (weekDiff >= 0 && weekDiff % n === 0) keys.add(dayKey);
      }
    }
  }
  return keys;
}

function rangeDays(r: ParsedReminder): number {
  if (!r.endDate || r.endDate <= r.date) return 1;
  const ms = new Date(r.endDate + 'T00:00:00').getTime() - new Date(r.date + 'T00:00:00').getTime();
  return Math.round(ms / 86_400_000) + 1;
}

type RangePos = 'only' | 'start' | 'mid' | 'end';

function getRangePos(r: ParsedReminder, dayKey: string): RangePos {
  if (!r.endDate || r.endDate <= r.date) return 'only';
  if (dayKey === r.date) return 'start';
  if (dayKey === r.endDate) return 'end';
  return 'mid';
}

function formatDisplayDate(dateKey: string): string {
  const d = new Date(dateKey + 'T00:00:00');
  return d.toLocaleDateString('default', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
}

const CalendarView: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const categories = useSelector((s: RootState) => s.categories.items);
  const nonEncryptedCats = categories.filter(c => !c.isEncrypted);

  const [mode, setMode] = useState<CalendarMode>(() =>
    (localStorage.getItem('lv_cal_mode') as CalendarMode) ?? 'month'
  );
  const [anchor, setAnchor] = useState(() => new Date());
  const [reminders, setReminders] = useState<ParsedReminder[]>([]);
  const [loading, setLoading] = useState(true);

  // Create reminder modal state
  const [createDate, setCreateDate]       = useState<string>('');
  const [createEndDate, setCreateEndDate] = useState<string>('');
  const [createCatId, setCreateCatId]     = useState<string>('');
  const [createTitle, setCreateTitle]     = useState<string>('');
  const [createTag, setCreateTag]         = useState<ReminderTag>('custom');
  const [creating, setCreating]           = useState(false);
  const [createError, setCreateError]     = useState('');

  const showCreate = createDate !== '';

  const openCreate = useCallback((dateKey: string) => {
    setCreateDate(dateKey);
    setCreateEndDate('');
    setCreateTitle('');
    setCreateTag('custom');
    setCreateCatId(nonEncryptedCats[0]?.id ?? '');
    setCreateError('');
  }, [nonEncryptedCats]);

  const closeCreate = useCallback(() => {
    setCreateDate('');
    setCreateEndDate('');
    setCreating(false);
    setCreateError('');
  }, []);

  const refreshReminders = useCallback(() => {
    remindersApi.getAll().then(({ data }) => {
      setReminders(data.flatMap((r: ReminderPage) => {
        try {
          const c: ReminderContent = JSON.parse(r.content);
          if (!c.date) return [];
          return [{
            pageId: r.pageId,
            categoryId: r.categoryId,
            categoryName: r.categoryName,
            categoryIcon: r.categoryIcon,
            title: r.title,
            date: c.date,
            endDate: c.endDate,
            tag: c.tag ?? 'custom',
            recurrence: c.recurrence ?? 'once',
            recurrenceInterval: c.recurrenceInterval ?? 1,
          }];
        } catch { return []; }
      }));
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  useEffect(() => { refreshReminders(); }, [refreshReminders]);

  const handleCreate = useCallback(async () => {
    if (!createCatId) { setCreateError('Please select a vault.'); return; }
    if (createEndDate && createEndDate < createDate) {
      setCreateError('End date must be on or after the start date.');
      return;
    }
    const isRange = !!createEndDate && createEndDate > createDate;
    const title = createTitle.trim() || (isRange ? `${createDate} – ${createEndDate}` : `Reminder – ${createDate}`);
    const content: ReminderContent = {
      date: createDate,
      ...(isRange ? { endDate: createEndDate } : {}),
      tag: createTag,
      recurrence: 'once',
      notes: '',
      notifyEnabled: false,
      notifyBefore: 1,
      notifyUnit: 'days',
    };
    setCreating(true);
    setCreateError('');
    try {
      const { data: newPage } = await pagesApi.create(createCatId, {
        title,
        type: 'Reminder',
        content: JSON.stringify(content),
        isEncrypted: false,
      });
      closeCreate();
      refreshReminders();
      dispatch(selectCategory(createCatId));
      dispatch(setPendingSelect(newPage.id));
    } catch {
      setCreateError('Failed to create reminder. Try again.');
    } finally {
      setCreating(false);
    }
  }, [createCatId, createTitle, createDate, createEndDate, createTag, closeCreate, refreshReminders, dispatch]);

  const navigate = useCallback((result: ParsedReminder) => {
    dispatch(selectCategory(result.categoryId));
    dispatch(setPendingSelect(result.pageId));
  }, [dispatch]);

  const changeMode = (m: CalendarMode) => {
    setMode(m);
    localStorage.setItem('lv_cal_mode', m);
  };

  const today = new Date();
  const todayKey = toDateKey(today);

  // ── Create reminder modal ───────────────────────────────────
  const createModal = showCreate && (
    <div className="cal-modal-overlay" onClick={closeCreate}>
      <div className="cal-modal" onClick={e => e.stopPropagation()}>
        <div className="cal-modal-header">
          <span className="cal-modal-title">🔔 New Reminder</span>
          <button className="cal-modal-close" onClick={closeCreate}>✕</button>
        </div>

        <div className="cal-modal-dates">
          <div className="cal-modal-date-col">
            <label className="cal-modal-label">Start Date</label>
            <input
              className="cal-modal-input"
              type="date"
              value={createDate}
              onChange={e => {
                setCreateDate(e.target.value);
                if (createEndDate && createEndDate < e.target.value) setCreateEndDate('');
              }}
            />
          </div>
          <div className="cal-modal-date-sep">→</div>
          <div className="cal-modal-date-col">
            <label className="cal-modal-label">End Date <span className="cal-modal-optional">(optional)</span></label>
            <input
              className="cal-modal-input"
              type="date"
              value={createEndDate}
              min={createDate}
              onChange={e => setCreateEndDate(e.target.value)}
            />
          </div>
        </div>

        <label className="cal-modal-label">Vault</label>
        {nonEncryptedCats.length === 0 ? (
          <p className="cal-modal-hint">No unencrypted vaults found. Create a vault first.</p>
        ) : (
          <select
            className="cal-modal-select"
            value={createCatId}
            onChange={e => setCreateCatId(e.target.value)}
          >
            {nonEncryptedCats.map(c => (
              <option key={c.id} value={c.id}>{c.icon} {c.name}</option>
            ))}
          </select>
        )}

        <label className="cal-modal-label">Title <span className="cal-modal-optional">(optional)</span></label>
        <input
          className="cal-modal-input"
          type="text"
          placeholder={`Reminder – ${createDate}`}
          value={createTitle}
          onChange={e => setCreateTitle(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleCreate()}
          autoFocus
        />

        <label className="cal-modal-label">Tag</label>
        <select
          className="cal-modal-select"
          value={createTag}
          onChange={e => setCreateTag(e.target.value as ReminderTag)}
        >
          {TAG_OPTIONS.map(o => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>

        {createError && <p className="cal-modal-error">{createError}</p>}

        <div className="cal-modal-actions">
          <button className="cal-modal-cancel" onClick={closeCreate}>Cancel</button>
          <button
            className="cal-modal-create"
            onClick={handleCreate}
            disabled={creating || nonEncryptedCats.length === 0}
          >
            {creating ? 'Creating…' : 'Create Reminder'}
          </button>
        </div>
      </div>
    </div>
  );

  // ── Month view ─────────────────────────────────────────────
  if (mode === 'month') {
    const year = anchor.getFullYear();
    const month = anchor.getMonth();
    const allDays = getMonthGrid(year, month).flat().filter(Boolean) as Date[];
    const grid = getMonthGrid(year, month);
    const flatGrid = grid.flat();

    const eventMap = new Map<string, ParsedReminder[]>();
    for (const r of reminders) {
      const keys = getOccurrencesInRange(r, allDays);
      for (const k of keys) {
        if (!eventMap.has(k)) eventMap.set(k, []);
        eventMap.get(k)!.push(r);
      }
    }

    const holidays = getUSHolidays(year);
    const monthLabel = anchor.toLocaleString('default', { month: 'long', year: 'numeric' });

    return (
      <>
        {createModal}
        <div className="cal-root">
          <div className="cal-header">
            <div className="cal-mode-btns">
              <button className="cal-mode-btn" onClick={() => changeMode('week')}>Week</button>
              <button className="cal-mode-btn cal-mode-btn--active" onClick={() => changeMode('month')}>Month</button>
            </div>
            <div className="cal-nav">
              <button className="cal-nav-btn" onClick={() => setAnchor(new Date(year, month - 1, 1))}>‹</button>
              <span className="cal-nav-label">{monthLabel}</span>
              <button className="cal-nav-btn" onClick={() => setAnchor(new Date(year, month + 1, 1))}>›</button>
            </div>
            <button className="cal-today-btn" onClick={() => setAnchor(new Date())}>Today</button>
          </div>

          <div className="cal-dow-row">
            {DOW.map(d => <div key={d} className="cal-dow">{d}</div>)}
          </div>

          <div className="cal-month-grid">
            {flatGrid.map((day, i) => {
              if (!day) return <div key={i} className="cal-day cal-day--empty" />;
              const key = toDateKey(day);
              const events = eventMap.get(key) ?? [];
              const holiday = holidays.get(key);
              const isToday = key === todayKey;
              const isOtherMonth = day.getMonth() !== month;
              return (
                <div key={key} className={`cal-day${isToday ? ' cal-day--today' : ''}${isOtherMonth ? ' cal-day--dim' : ''}`}>
                  <div className="cal-day-top">
                    <span className="cal-day-num">{day.getDate()}</span>
                    <button
                      className="cal-add-btn"
                      onClick={() => openCreate(key)}
                      title={`Add reminder for ${key}`}
                    >+</button>
                  </div>
                  <div className="cal-day-events">
                    {holiday && <div className="cal-holiday">{holiday}</div>}
                    {events.map(r => {
                      const pos = getRangePos(r, key);
                      const days = rangeDays(r);
                      return (
                        <button
                          key={r.pageId}
                          className={`cal-event cal-event--${pos}`}
                          style={{ '--event-color': TAG_COLORS[r.tag] ?? TAG_COLORS.custom } as React.CSSProperties}
                          onClick={() => navigate(r)}
                          title={`${r.title} — ${r.categoryIcon} ${r.categoryName}${days > 1 ? ` (${days} days)` : ''}`}
                        >
                          {pos !== 'mid' && <span className="cal-event-icon">{TAG_ICONS[r.tag]}</span>}
                          <span className="cal-event-title">
                            {pos === 'start' ? `${r.title} →` : pos === 'end' ? `← ${r.title}` : pos === 'mid' ? r.title : r.title}
                          </span>
                          {pos === 'start' && days > 1 && <span className="cal-event-days">{days}d</span>}
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
          {loading && <div className="cal-loading">Loading reminders…</div>}
        </div>
      </>
    );
  }

  // ── Week view ──────────────────────────────────────────────
  const weekDays = getWeekDays(anchor);
  const weekHolidays = new Map([
    ...getUSHolidays(weekDays[0].getFullYear()).entries(),
    ...getUSHolidays(weekDays[6].getFullYear()).entries(),
  ]);
  const eventMap = new Map<string, ParsedReminder[]>();
  for (const r of reminders) {
    const keys = getOccurrencesInRange(r, weekDays);
    for (const k of keys) {
      if (!eventMap.has(k)) eventMap.set(k, []);
      eventMap.get(k)!.push(r);
    }
  }
  const weekStart = weekDays[0];
  const weekEnd = weekDays[6];
  const weekLabel = `${weekStart.toLocaleDateString('default', { month: 'short', day: 'numeric' })} – ${weekEnd.toLocaleDateString('default', { month: 'short', day: 'numeric', year: 'numeric' })}`;

  return (
    <>
      {createModal}
      <div className="cal-root">
        <div className="cal-header">
          <div className="cal-mode-btns">
            <button className="cal-mode-btn cal-mode-btn--active" onClick={() => changeMode('week')}>Week</button>
            <button className="cal-mode-btn" onClick={() => changeMode('month')}>Month</button>
          </div>
          <div className="cal-nav">
            <button className="cal-nav-btn" onClick={() => { const d = new Date(anchor); d.setDate(d.getDate() - 7); setAnchor(d); }}>‹</button>
            <span className="cal-nav-label">{weekLabel}</span>
            <button className="cal-nav-btn" onClick={() => { const d = new Date(anchor); d.setDate(d.getDate() + 7); setAnchor(d); }}>›</button>
          </div>
          <button className="cal-today-btn" onClick={() => setAnchor(new Date())}>Today</button>
        </div>

        <div className="cal-week-grid">
          {weekDays.map(day => {
            const key = toDateKey(day);
            const events = eventMap.get(key) ?? [];
            const holiday = weekHolidays.get(key);
            const isToday = key === todayKey;
            return (
              <div key={key} className={`cal-week-col${isToday ? ' cal-week-col--today' : ''}`}>
                <div className="cal-week-col-header">
                  <span className="cal-week-dow">{DOW[day.getDay()]}</span>
                  <span className={`cal-week-date${isToday ? ' cal-week-date--today' : ''}`}>{day.getDate()}</span>
                  <button
                    className="cal-add-btn cal-add-btn--week"
                    onClick={() => openCreate(key)}
                    title={`Add reminder for ${key}`}
                  >+</button>
                </div>
                <div className="cal-week-events">
                  {holiday && <div className="cal-holiday cal-holiday--week">{holiday}</div>}
                  {events.map(r => {
                    const pos = getRangePos(r, key);
                    const days = rangeDays(r);
                    return (
                      <button
                        key={r.pageId}
                        className={`cal-event cal-event--week cal-event--${pos}`}
                        style={{ '--event-color': TAG_COLORS[r.tag] ?? TAG_COLORS.custom } as React.CSSProperties}
                        onClick={() => navigate(r)}
                        title={`${r.title}${days > 1 ? ` (${days} days)` : ''} — ${r.categoryIcon} ${r.categoryName}`}
                      >
                        {pos !== 'mid' && <span className="cal-event-icon">{TAG_ICONS[r.tag]}</span>}
                        <span className="cal-event-title">
                          {pos === 'start' ? `${r.title} →` : pos === 'end' ? `← ${r.title}` : r.title}
                        </span>
                        {pos === 'only' && <span className="cal-event-cat">{r.categoryIcon} {r.categoryName}</span>}
                      </button>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
        {loading && <div className="cal-loading">Loading reminders…</div>}
      </div>
    </>
  );
};

export default CalendarView;
