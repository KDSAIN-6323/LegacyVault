import React from 'react';
import type { ReminderContent, ReminderTag, ReminderRecurrence, NotifyUnit } from '../../types';
import { requestNotificationPermission, notificationsSupported } from '../../services/notificationService';
import './ReminderEditor.css';

interface Props {
  content: ReminderContent;
  onChange: (content: ReminderContent) => void;
}

const TAG_LABELS: Record<ReminderTag, string> = {
  birthday:    '🎂 Birthday',
  anniversary: '💍 Anniversary',
  holiday:     '🎉 Holiday',
  appointment: '📅 Appointment',
  custom:      '🔔 Custom',
};

const RECURRENCE_LABELS: Record<ReminderRecurrence, string> = {
  once:    'One-time',
  weekly:  'Weekly',
  monthly: 'Monthly',
  yearly:  'Yearly',
};

function daysUntilNext(
  dateStr: string,
  recurrence: ReminderRecurrence,
  interval: number = 1,
): number | null {
  if (!dateStr) return null;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const base = new Date(dateStr + 'T00:00:00');
  const n = Math.max(1, interval);

  if (recurrence === 'once') {
    return Math.ceil((base.getTime() - today.getTime()) / 86_400_000);
  }

  let next: Date;

  if (recurrence === 'yearly') {
    const yearDiff = today.getFullYear() - base.getFullYear();
    const cycles = Math.ceil(yearDiff / n);
    next = new Date(base);
    next.setFullYear(base.getFullYear() + cycles * n);
    if (next < today) next.setFullYear(next.getFullYear() + n);
  } else if (recurrence === 'monthly') {
    const monthDiff = (today.getFullYear() - base.getFullYear()) * 12 + (today.getMonth() - base.getMonth());
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

  return Math.ceil((next.getTime() - today.getTime()) / 86_400_000);
}

function formatCountdown(
  days: number | null,
  endDate?: string,
): { label: string; className: string } {
  if (days === null) return { label: '', className: '' };

  // Range: check if we're currently within it
  if (endDate) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const end = new Date(endDate + 'T00:00:00');
    const daysLeft = Math.ceil((end.getTime() - today.getTime()) / 86_400_000);
    if (days <= 0 && daysLeft >= 0) {
      if (daysLeft === 0) return { label: 'Last day!', className: 'countdown--today' };
      return { label: `In progress · ${daysLeft} day${daysLeft !== 1 ? 's' : ''} remaining`, className: 'countdown--today' };
    }
    if (daysLeft < 0) {
      const ago = Math.abs(daysLeft);
      return { label: `Ended ${ago} day${ago !== 1 ? 's' : ''} ago`, className: 'countdown--past' };
    }
  }

  if (days < 0)  return { label: `${Math.abs(days)} day${Math.abs(days) !== 1 ? 's' : ''} ago`, className: 'countdown--past' };
  if (days === 0) return { label: 'Today!', className: 'countdown--today' };
  if (days === 1) return { label: 'Tomorrow', className: 'countdown--soon' };
  if (days <= 7)  return { label: `In ${days} days`, className: 'countdown--soon' };
  if (days <= 30) return { label: `In ${days} days`, className: 'countdown--upcoming' };
  return { label: `In ${days} days`, className: 'countdown--future' };
}

const ReminderEditor: React.FC<Props> = ({ content, onChange }) => {
  const update = (patch: Partial<ReminderContent>) => onChange({ ...content, ...patch });

  const handleNotifyToggle = async (enabled: boolean) => {
    if (enabled && notificationsSupported()) {
      const permission = await requestNotificationPermission();
      if (permission !== 'granted') {
        update({ notifyEnabled: false });
        return;
      }
    }
    update({ notifyEnabled: enabled });
  };

  const interval = content.recurrenceInterval ?? 1;
  const days = daysUntilNext(content.date, content.recurrence, interval);
  const countdown = formatCountdown(days, content.endDate);
  const isRange = content.recurrence === 'once';

  const unitLabel = (n: number) => {
    if (content.recurrence === 'weekly')  return n === 1 ? 'week'  : 'weeks';
    if (content.recurrence === 'monthly') return n === 1 ? 'month' : 'months';
    if (content.recurrence === 'yearly')  return n === 1 ? 'year'  : 'years';
    return '';
  };

  return (
    <div className="reminder-editor">
      {/* Countdown banner */}
      {content.date && (
        <div className={`reminder-countdown ${countdown.className}`}>
          <span className="reminder-countdown-icon">
            {days === 0 ? '🎉' : days !== null && days < 0 ? '⏮' : '⏳'}
          </span>
          <span>{countdown.label}</span>
        </div>
      )}

      <div className="reminder-grid">
        {/* Start Date */}
        <div className="rm-field">
          <label>{isRange ? 'Start Date' : 'Date'}</label>
          <input
            type="date"
            value={content.date}
            onChange={(e) => {
              const next: Partial<ReminderContent> = { date: e.target.value };
              if (content.endDate && content.endDate < e.target.value) next.endDate = '';
              update(next);
            }}
          />
        </div>

        {/* End Date — only for one-time events */}
        {isRange && (
          <div className="rm-field">
            <label>End Date <span className="rm-optional">(optional)</span></label>
            <input
              type="date"
              value={content.endDate ?? ''}
              min={content.date || undefined}
              onChange={(e) => update({ endDate: e.target.value || undefined })}
            />
          </div>
        )}

        {/* Tag */}
        <div className="rm-field">
          <label>Type</label>
          <select value={content.tag} onChange={(e) => update({ tag: e.target.value as ReminderTag })}>
            {(Object.keys(TAG_LABELS) as ReminderTag[]).map((t) => (
              <option key={t} value={t}>{TAG_LABELS[t]}</option>
            ))}
          </select>
        </div>

        {/* Recurrence */}
        <div className="rm-field">
          <label>Repeats</label>
          <select
            value={content.recurrence}
            onChange={(e) => {
              const rec = e.target.value as ReminderRecurrence;
              update({
                recurrence: rec,
                ...(rec === 'once' ? { endDate: undefined, recurrenceInterval: undefined } : {}),
              });
            }}
          >
            {(Object.keys(RECURRENCE_LABELS) as ReminderRecurrence[]).map((r) => (
              <option key={r} value={r}>{RECURRENCE_LABELS[r]}</option>
            ))}
          </select>
        </div>

        {/* Interval — only shown for recurring types */}
        {content.recurrence !== 'once' && (
          <div className="rm-field">
            <label>Every</label>
            <div className="rm-interval-row">
              <input
                type="number"
                min={1}
                max={99}
                value={interval}
                onChange={(e) => update({ recurrenceInterval: Math.max(1, parseInt(e.target.value) || 1) })}
                className="rm-interval-input"
              />
              <span className="rm-interval-unit">{unitLabel(interval)}</span>
            </div>
          </div>
        )}
      </div>

      {/* Notifications */}
      <div className="rm-notify-section">
        <label className="rm-notify-toggle">
          <input
            type="checkbox"
            checked={content.notifyEnabled ?? false}
            disabled={!notificationsSupported()}
            onChange={(e) => handleNotifyToggle(e.target.checked)}
          />
          <span>
            {notificationsSupported()
              ? 'Enable notifications'
              : 'Notifications not supported in this browser'}
          </span>
        </label>

        {content.notifyEnabled && (
          <div className="rm-notify-timing">
            <span className="rm-notify-label">Remind me</span>
            <input
              type="number"
              min={1}
              max={999}
              value={content.notifyBefore ?? 1}
              onChange={(e) => update({ notifyBefore: Math.max(1, parseInt(e.target.value) || 1) })}
              className="rm-notify-number"
            />
            <select
              value={content.notifyUnit ?? 'days'}
              onChange={(e) => update({ notifyUnit: e.target.value as NotifyUnit })}
            >
              <option value="hours">hours before</option>
              <option value="days">days before</option>
              <option value="weeks">weeks before</option>
            </select>
          </div>
        )}
      </div>

      {/* Notes */}
      <div className="rm-field rm-field--grow">
        <label>Notes</label>
        <textarea
          value={content.notes}
          placeholder="Any extra details..."
          rows={5}
          onChange={(e) => update({ notes: e.target.value })}
        />
      </div>
    </div>
  );
};

export default ReminderEditor;
