import React, { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { fetchCategories } from '../../store/categoriesSlice';
import { fetchPages, selectPage, setPendingSelect } from '../../store/pagesSlice';
import { logoutUser } from '../../store/authSlice';
import { keyCache } from '../../crypto/keyCache';
import Sidebar from './Sidebar';
import PageList from './PageList';
import ContentArea from './ContentArea';
import { useReminderNotifications } from '../../hooks/useReminderNotifications';
import { useInactivityLogout } from '../../hooks/useInactivityLogout';
import { useTheme } from '../../hooks/useTheme';
import { useFontSize } from '../../hooks/useFontSize';
import type { FontSize } from '../../hooks/useFontSize';
import GlobalSearch from './GlobalSearch';
import { backupApi } from '../../api/backupApi';
import type { BackupEntry } from '../../api/backupApi';
import './AppShell.css';

const APP_VERSION = '1.0.0';

const BrandIcon = () => (
  <svg width="22" height="22" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
    <circle cx="24" cy="24" r="16.5" stroke="currentColor" strokeWidth="2.5"/>
    <circle cx="24"   cy="5.5"  r="3"   fill="currentColor"/>
    <circle cx="42.5" cy="24"   r="3"   fill="currentColor"/>
    <circle cx="24"   cy="42.5" r="3"   fill="currentColor"/>
    <circle cx="5.5"  cy="24"   r="3"   fill="currentColor"/>
    <circle cx="24" cy="24" r="9" stroke="currentColor" strokeWidth="1.8"/>
    <circle cx="24" cy="24" r="3.5" fill="currentColor"/>
    <line x1="24" y1="24" x2="30.5" y2="20.2" stroke="var(--color-accent-subtle, #a78bfa)" strokeWidth="2" strokeLinecap="round"/>
  </svg>
);

const CogIcon = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
    <circle cx="12" cy="12" r="3"/>
    <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
  </svg>
);

const LogoutIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <line x1="2" y1="2" x2="14" y2="14" stroke="#ef4444" strokeWidth="2.2" strokeLinecap="round"/>
    <line x1="14" y1="2" x2="2" y2="14" stroke="#ef4444" strokeWidth="2.2" strokeLinecap="round"/>
  </svg>
);

const HelpModal: React.FC<{ onClose: () => void }> = ({ onClose }) => (
  <div className="help-overlay" onMouseDown={onClose}>
    <div className="help-modal" onMouseDown={e => e.stopPropagation()}>
      <div className="help-modal-header">
        <div className="help-modal-title">
          <BrandIcon />
          <span>Legacy Vault — Help</span>
        </div>
        <button className="help-modal-close" onClick={onClose} title="Close">✕</button>
      </div>

      <div className="help-modal-body">
        <p className="help-version">Version {APP_VERSION}</p>
        <p className="help-description">
          Legacy Vault is a personal digital preservation app — a secure, private place to store your recipes,
          quotes, notes, home inventory, passwords, reminders, and shopping lists across organised shelves.
        </p>

        <div className="help-section help-section--warning">
          <h3>Important Password Warnings</h3>
          <ul>
            <li><strong>Login password:</strong> If you forget it, use "Forgot password?" on the sign-in screen. You will need your username and registered email address to reset it.</li>
            <li><strong>Safe / encrypted shelf passwords:</strong> These are used to encrypt your data with AES-256-GCM. <strong>If you forget this password, your encrypted data cannot be recovered — there is no backdoor.</strong> You can set an optional hint when creating the safe to help you remember.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Vaults</h3>
          <ul>
            <li><strong>📁 Shelf</strong> — A general-purpose container for any page types. Can optionally be encrypted with a password.</li>
            <li><strong>🔑 Safe</strong> — Always-encrypted vault designed for passwords and sensitive data. Requires a master password every session.</li>
            <li>Right-click or use the <strong>⋮</strong> menu on any shelf to rename or delete it.</li>
            <li>Deleting a shelf permanently deletes all pages inside it.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Pages</h3>
          <ul>
            <li><strong>📝 Note</strong> — Rich-text notes with full formatting support.</li>
            <li><strong>💬 Quote</strong> — Memorable quotes with author, source, and searchable tags.</li>
            <li><strong>🍽️ Recipe</strong> — Ingredient lists and step-by-step instructions. Use the 🛒 button to add ingredients to a shopping list.</li>
            <li><strong>🛒 Shopping List</strong> — Checklist of items with quantities. Check off items as you shop.</li>
            <li><strong>🏠 Home Inventory</strong> — Item records with value, serial number, warranty, and photo attachments.</li>
            <li><strong>🔔 Reminder</strong> — Date-based reminders with recurrence (once, weekly, monthly, yearly) and browser notifications.</li>
            <li><strong>🔑 Password</strong> — Encrypted credential entry (URL, username, password, TOTP). Only available inside a Safe vault.</li>
            <li>Right-click or use <strong>⋮</strong> on any page to rename, move to another shelf, or delete it.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Backup &amp; Restore</h3>
          <ul>
            <li>Access via Settings &gt; <strong>Backup &amp; Restore</strong>.</li>
            <li>Click <strong>Create Backup</strong> and enter your login password. The backup is encrypted with AES-256-GCM before being saved to the server.</li>
            <li>To restore, click <strong>Restore</strong> next to any backup file, enter the password used when it was created, and confirm. The app will restart after restoring.</li>
            <li>Backup files use the <code>.vaultbak</code> extension and are stored on the server.</li>
            <li>If you change your login password, older backups still use the password that was set when they were created.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Themes &amp; Appearance</h3>
          <ul>
            <li>Click the <strong>sun / moon icon</strong> in the top bar to toggle between light and dark mode.</li>
            <li>Use the <strong>A / A</strong> font-size buttons to make text larger or smaller.</li>
            <li>Your theme and font-size preferences are saved in your browser and persist across sessions.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Search</h3>
          <ul>
            <li>Use the <strong>search bar</strong> (magnifier icon, top bar) to search across all shelves and pages globally.</li>
            <li>Use the <strong>in-shelf search</strong> box to filter pages within the currently open shelf.</li>
            <li>Search matches page titles, content, tags, ingredients, and shopping list items.</li>
          </ul>
        </div>

        <div className="help-section">
          <h3>Built with</h3>
          <ul>
            <li>React 19 · TypeScript · Redux Toolkit · Tiptap</li>
            <li>ASP.NET Core 10 · Entity Framework Core · SQLite</li>
            <li>AES-256-GCM encryption · Docker</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
);

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatBackupDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

const BackupModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const [backups, setBackups] = React.useState<BackupEntry[] | null>(null);
  const [creating, setCreating] = React.useState(false);
  const [createPassword, setCreatePassword] = React.useState('');
  const [restoring, setRestoring] = React.useState<string | null>(null);
  const [restoreTarget, setRestoreTarget] = React.useState<BackupEntry | null>(null);
  const [restorePassword, setRestorePassword] = React.useState('');
  const [status, setStatus] = React.useState<{ ok: boolean; msg: string } | null>(null);

  React.useEffect(() => {
    backupApi.list().then((r) => setBackups(r.data)).catch(() => setBackups([]));
  }, []);

  const handleCreate = async () => {
    if (!createPassword.trim()) return;
    setCreating(true);
    setStatus(null);
    try {
      const { data } = await backupApi.create(createPassword);
      setBackups((prev) => (prev ? [data, ...prev] : [data]));
      setStatus({ ok: true, msg: `Backup created: ${data.fileName}` });
      setCreatePassword('');
    } catch {
      setStatus({ ok: false, msg: 'Backup failed. Check server logs.' });
    } finally {
      setCreating(false);
    }
  };

  const handleRestoreConfirm = async () => {
    if (!restoreTarget || !restorePassword.trim()) return;
    setRestoring(restoreTarget.fileName);
    setStatus(null);
    try {
      await backupApi.restore(restoreTarget.fileName, restorePassword);
      setStatus({ ok: true, msg: 'Database restored successfully.' });
      setRestoreTarget(null);
      setRestorePassword('');
    } catch {
      setStatus({ ok: false, msg: 'Restore failed — check your password and try again.' });
    } finally {
      setRestoring(null);
    }
  };

  const cancelRestore = () => { setRestoreTarget(null); setRestorePassword(''); };

  return (
    <div className="help-overlay" onMouseDown={onClose}>
      <div className="backup-modal" onMouseDown={(e) => e.stopPropagation()}>
        <div className="backup-modal-header">
          <div className="backup-modal-title">💾 Backup &amp; Restore</div>
          <button className="backup-modal-close" onClick={onClose} title="Close">✕</button>
        </div>

        <div className="backup-modal-body">
          <div className="backup-action-row">
            <input
              type="password"
              className="backup-password-input"
              placeholder="Password to encrypt backup…"
              value={createPassword}
              onChange={(e) => setCreatePassword(e.target.value)}
              onKeyDown={(e) => { if (e.key === 'Enter') handleCreate(); }}
              disabled={creating || restoring !== null}
            />
            <button
              className="backup-create-btn"
              onClick={handleCreate}
              disabled={creating || !createPassword.trim() || restoring !== null}
            >
              {creating ? 'Creating…' : '+ Create Backup'}
            </button>
          </div>
          {status && (
            <span className={`backup-status ${status.ok ? 'backup-status--ok' : 'backup-status--err'}`}>
              {status.msg}
            </span>
          )}

          <p className="backup-restore-notice">
            Backups are encrypted with AES-256-GCM and saved to <strong>Documents/.vault/backups</strong>.
            You will need this password to restore.
          </p>

          {backups === null ? (
            <p className="backup-empty">Loading…</p>
          ) : backups.length === 0 ? (
            <p className="backup-empty">No backups yet. Enter a password above and click <em>Create Backup</em>.</p>
          ) : (
            <>
              <span className="backup-list-heading">Backups — {backups.length} file{backups.length !== 1 ? 's' : ''}</span>
              <div className="backup-list">
                {backups.map((b) => (
                  <div key={b.fileName} className="backup-item">
                    <div className="backup-item-info">
                      <span className="backup-item-date">{formatBackupDate(b.createdAt)}</span>
                      <span className="backup-item-meta">{b.fileName} · {formatBytes(b.fileSizeBytes)}</span>
                    </div>
                    {restoreTarget?.fileName === b.fileName ? (
                      <div className="backup-restore-form">
                        <input
                          type="password"
                          className="backup-password-input"
                          placeholder="Backup password…"
                          value={restorePassword}
                          autoFocus
                          onChange={(e) => setRestorePassword(e.target.value)}
                          onKeyDown={(e) => {
                            if (e.key === 'Enter') handleRestoreConfirm();
                            if (e.key === 'Escape') cancelRestore();
                          }}
                          disabled={restoring !== null}
                        />
                        <button
                          className="backup-restore-btn"
                          onClick={handleRestoreConfirm}
                          disabled={!restorePassword.trim() || restoring !== null}
                        >
                          {restoring === b.fileName ? 'Restoring…' : 'Confirm'}
                        </button>
                        <button
                          className="backup-cancel-btn"
                          onClick={cancelRestore}
                          disabled={restoring !== null}
                        >
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <button
                        className="backup-restore-btn"
                        onClick={() => { setRestoreTarget(b); setRestorePassword(''); setStatus(null); }}
                        disabled={creating || restoring !== null}
                      >
                        Restore
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

const AppShell: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const selectedCategoryId = useSelector((s: RootState) => s.categories.selectedId);
  const selectedPageId = useSelector((s: RootState) => s.pages.selectedId);
  const pendingSelectId = useSelector((s: RootState) => s.pages.pendingSelectId);
  const currentCategoryId = useSelector((s: RootState) => s.pages.currentCategoryId);
  const user = useSelector((s: RootState) => s.auth.user);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Page shelf collapse logic — fully derived, no effects needed.
  // null  → auto mode: collapsed iff no vault is selected.
  // ''    → user closed it while no vault was selected.
  // id    → user closed it while that vault was selected.
  // '__lv_open__' → user explicitly opened it (overrides auto).
  const [pageShelfClosedFor, setPageShelfClosedFor] = useState<string | null>(null);
  const pageListCollapsed =
    pageShelfClosedFor === null
      ? !selectedCategoryId
      : pageShelfClosedFor === (selectedCategoryId ?? '');
  const handlePageListToggle = () =>
    setPageShelfClosedFor(pageListCollapsed ? '__lv_open__' : (selectedCategoryId ?? ''));
  const [calendarVisible, setCalendarVisible] = useState(() =>
    localStorage.getItem('lv_calendar') === 'true'
  );
  const toggleCalendar = () => {
    setCalendarVisible(v => {
      localStorage.setItem('lv_calendar', String(!v));
      return !v;
    });
  };
  const [theme, toggleTheme] = useTheme();
  const [fontSize, setFontSize] = useFontSize();
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [helpOpen, setHelpOpen] = useState(false);
  const [backupOpen, setBackupOpen] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);

  // Keep a ref so the category-change effect can read the latest pendingSelectId
  // without adding it as a dependency (which would cause duplicate fetches).
  const pendingSelectRef = useRef(pendingSelectId);
  useLayoutEffect(() => { pendingSelectRef.current = pendingSelectId; });

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  useEffect(() => {
    if (selectedCategoryId) {
      dispatch(fetchPages(selectedCategoryId));
      if (!pendingSelectRef.current) {
        dispatch(selectPage(null));
      }
      // Collapse sidebar, expand page list when a vault is selected
      setSidebarCollapsed(true);
      setPageShelfClosedFor('__lv_open__');
    }
  }, [selectedCategoryId, dispatch]);

  // If search targets a page in the already-loaded category, consume pendingSelectId immediately.
  useEffect(() => {
    if (pendingSelectId && currentCategoryId === selectedCategoryId) {
      dispatch(selectPage(pendingSelectId));
      dispatch(setPendingSelect(null));
      setPageShelfClosedFor('__lv_open__');
    }
  }, [pendingSelectId, currentCategoryId, selectedCategoryId, dispatch]);

  // Collapse page list when a page is selected
  useEffect(() => {
    if (selectedPageId) {
      setPageShelfClosedFor(selectedCategoryId ?? '');
    }
  }, [selectedPageId, selectedCategoryId]);


  // Close settings panel on outside click or Escape
  useEffect(() => {
    if (!settingsOpen) return;
    const onMouseDown = (e: MouseEvent) => {
      if (settingsRef.current && !settingsRef.current.contains(e.target as Node)) {
        setSettingsOpen(false);
      }
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setSettingsOpen(false);
    };
    document.addEventListener('mousedown', onMouseDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onMouseDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [settingsOpen]);

  useReminderNotifications();
  useInactivityLogout();

  const handleLogout = async () => {
    keyCache.clearAll();
    await dispatch(logoutUser());
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <span className="app-header-logo"><BrandIcon /> Legacy Vault</span>
        <GlobalSearch />
        <div className="app-header-actions">
          <span className="app-header-user">{user?.username}</span>

          <div className="settings-wrapper" ref={settingsRef}>
            <button
              className={`settings-cog-btn${settingsOpen ? ' settings-cog-btn--open' : ''}`}
              onClick={() => setSettingsOpen(o => !o)}
              title="Settings"
            >
              <CogIcon />
            </button>

            {settingsOpen && (
              <div className="settings-panel">
                <div className="settings-section">
                  <span className="settings-label">Theme</span>
                  <div className="settings-toggle-group">
                    <button
                      className={`settings-option-btn${theme === 'dark' ? ' settings-option-btn--active' : ''}`}
                      onClick={() => theme !== 'dark' && toggleTheme()}
                    >
                      🌙 Dark
                    </button>
                    <button
                      className={`settings-option-btn${theme === 'light' ? ' settings-option-btn--active' : ''}`}
                      onClick={() => theme !== 'light' && toggleTheme()}
                    >
                      ☀ Light
                    </button>
                  </div>
                </div>

                <div className="settings-section">
                  <span className="settings-label">Font Size</span>
                  <div className="settings-toggle-group">
                    {(['small', 'medium', 'large'] as FontSize[]).map((s) => (
                      <button
                        key={s}
                        className={`settings-option-btn${fontSize === s ? ' settings-option-btn--active' : ''}`}
                        onClick={() => setFontSize(s)}
                      >
                        {s === 'small' ? 'A− Small' : s === 'large' ? 'A+ Large' : 'A  Medium'}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="settings-divider" />

                <button
                  className="settings-help-btn"
                  onClick={() => { setSettingsOpen(false); toggleCalendar(); }}
                >
                  {calendarVisible ? '📅 Hide Calendar' : '📅 Show Calendar'}
                </button>

                <button
                  className="settings-help-btn"
                  onClick={() => { setSettingsOpen(false); setBackupOpen(true); }}
                >
                  💾 Backup &amp; Restore
                </button>

                <button
                  className="settings-help-btn"
                  onClick={() => { setSettingsOpen(false); setHelpOpen(true); }}
                >
                  ? About Legacy Vault
                </button>
              </div>
            )}
          </div>

          <button className="app-header-logout" onClick={handleLogout} title="Logout">
            <LogoutIcon />
          </button>
        </div>
      </header>

      <div className="app-columns">
        <Sidebar collapsed={sidebarCollapsed} onToggle={() => setSidebarCollapsed(c => !c)} />
        <PageList collapsed={pageListCollapsed} onToggle={handlePageListToggle} />
        <ContentArea key={selectedPageId ?? 'none'} calendarVisible={calendarVisible} />
      </div>

      {backupOpen && <BackupModal onClose={() => setBackupOpen(false)} />}
      {helpOpen && <HelpModal onClose={() => setHelpOpen(false)} />}
    </div>
  );
};

export default AppShell;
