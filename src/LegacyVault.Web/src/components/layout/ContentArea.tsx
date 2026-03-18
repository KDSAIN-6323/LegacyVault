import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { savePage } from '../../store/pagesSlice';
import { unlockCategory } from '../../store/categoriesSlice';
import { cryptoService } from '../../crypto/cryptoService';
import { keyCache } from '../../crypto/keyCache';
import type { PageContent, NoteContent, RecipeContent, QuoteContent, HomeInventoryContent, PasswordContent, ReminderContent, ShoppingListContent } from '../../types';
import { pagesApi } from '../../api/pagesApi';
import NoteEditor from '../editors/NoteEditor';
import RecipeEditor from '../editors/RecipeEditor';
import QuoteEditor from '../editors/QuoteEditor';
import InventoryEditor from '../editors/InventoryEditor';
import PasswordEditor from '../editors/PasswordEditor';
import ReminderEditor from '../editors/ReminderEditor';
import ShoppingListEditor from '../editors/ShoppingListEditor';
import EncryptionPrompt from '../auth/EncryptionPrompt';
import CalendarView from '../calendar/CalendarView';
import './ContentArea.css';

// Module-level so it survives ContentArea remounts (keyed by selectedPageId in AppShell).
const lockedOverrides = new Map<string, boolean>();

const BrandIcon = () => (
  <svg width="72" height="72" viewBox="0 0 72 72" fill="none" xmlns="http://www.w3.org/2000/svg">
    <rect width="72" height="72" rx="12" fill="#1e1e2e"/>
    <circle cx="36" cy="36" r="26" stroke="#89b4fa" strokeWidth="2.5" fill="none"/>
    <circle cx="36" cy="12" r="4" fill="#89b4fa"/>
    <circle cx="36" cy="60" r="4" fill="#89b4fa"/>
    <circle cx="12" cy="36" r="4" fill="#89b4fa"/>
    <circle cx="60" cy="36" r="4" fill="#89b4fa"/>
    <circle cx="36" cy="36" r="14" stroke="#cba6f7" strokeWidth="2" fill="none"/>
    <circle cx="36" cy="36" r="5" fill="#cba6f7"/>
    <line x1="36" y1="31" x2="36" y2="24" stroke="#cba6f7" strokeWidth="2" strokeLinecap="round"/>
  </svg>
);

const ContentArea: React.FC<{ calendarVisible?: boolean }> = ({ calendarVisible = false }) => {
  const dispatch = useDispatch<AppDispatch>();
  const category = useSelector((s: RootState) =>
    s.categories.items.find((c) => c.id === s.categories.selectedId)
  );
  const page = useSelector((s: RootState) =>
    s.pages.items.find((p) => p.id === s.pages.selectedId)
  );

  const [content, setContent] = useState<PageContent | null>(null);
  const [title, setTitle] = useState('');
  const [needsPassword, setNeedsPassword] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [saving, setSaving] = useState(false);
  const saveInFlightRef = useRef(false);


  useEffect(() => {
    if (!page) { setContent(null); setTitle(''); return; }

    setTitle(page.title);
    setDirty(false);

    const applyLockOverride = (parsed: PageContent): PageContent => {
      if (!lockedOverrides.has(page.id)) return parsed;
      return { ...parsed, locked: lockedOverrides.get(page.id) } as PageContent;
    };

    const loadContent = async () => {
      // Always fetch fresh content for ShoppingList pages — items may have been
      // added externally (e.g. from a recipe) without updating the Redux store.
      let rawContent = page.content;
      let rawIV = page.encryptionIV;
      if (page.type === 'ShoppingList' && category) {
        try {
          const { data: fresh } = await pagesApi.getById(category.id, page.id);
          rawContent = fresh.content;
          rawIV = fresh.encryptionIV;
        } catch { /* fall back to store content */ }
      }

      if (page.isEncrypted && category?.encryptionSalt) {
        const key = keyCache.get(category.id);
        if (!key) { setNeedsPassword(true); return; }
        try {
          const plain = await cryptoService.decrypt(rawContent, rawIV!, key);
          setContent(applyLockOverride(JSON.parse(plain)));
          setNeedsPassword(false);
        } catch {
          setNeedsPassword(true);
        }
      } else {
        try { setContent(applyLockOverride(JSON.parse(rawContent))); } catch { setContent(null); }
        setNeedsPassword(false);
      }
    };

    loadContent();
  }, [page?.id]);

  const handlePasswordUnlock = useCallback(async (password: string) => {
    if (!category?.encryptionSalt || !page) return;
    const key = await cryptoService.deriveKey(password, category.encryptionSalt);
    try {
      const plain = await cryptoService.decrypt(page.content, page.encryptionIV!, key);
      keyCache.set(category.id, key);
      dispatch(unlockCategory(category.id));
      setContent(JSON.parse(plain));
      setNeedsPassword(false);
    } catch {
      throw new Error('Incorrect password');
    }
  }, [category, page]);

  const handleSave = useCallback(async (contentOverride?: PageContent) => {
    const saveContent = contentOverride ?? content;
    if (!page || !category || !saveContent) return;
    if (saveInFlightRef.current) return; // deduplicate concurrent saves
    saveInFlightRef.current = true;
    setSaving(true);
    try {
      let contentStr = JSON.stringify(saveContent);
      let encryptionIV: string | undefined;

      if (page.isEncrypted) {
        const key = keyCache.get(category.id);
        if (!key) { setNeedsPassword(true); return; }
        const encrypted = await cryptoService.encrypt(contentStr, key);
        contentStr = encrypted.ciphertext;
        encryptionIV = encrypted.iv;
      }

      await dispatch(savePage({
        categoryId: category.id,
        id: page.id,
        title,
        content: contentStr,
        encryptionIV,
      }));
      setDirty(false);
    } finally {
      setSaving(false);
      saveInFlightRef.current = false;
    }
  }, [page, category, content, title, dispatch]);

  // Auto-save on Ctrl+S
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') { e.preventDefault(); handleSave(); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [handleSave]);

  if (!page) {
    return (
      <main className={`content-area${calendarVisible ? ' content-area--calendar' : ' content-area--empty'}`}>
        <div className="content-brand">
          <BrandIcon />
          <h1 className="content-brand-title">Legacy Vault</h1>
          <p className="content-brand-tagline">What is your legacy?</p>
        </div>
        {calendarVisible && <CalendarView />}
      </main>
    );
  }

  if (needsPassword) {
    return (
      <main className="content-area">
        <EncryptionPrompt categoryName={category?.name ?? ''} passwordHint={category?.passwordHint} onUnlock={handlePasswordUnlock} />
      </main>
    );
  }

  const handleContentChange = (next: PageContent) => {
    setContent(next);
    setDirty(true);

    const lockChanged = (content as any)?.locked !== (next as any)?.locked;
    const tagsChanged = JSON.stringify((content as any)?.tags) !== JSON.stringify((next as any)?.tags);

    if (lockChanged || tagsChanged) {
      if (lockChanged && page) {
        // Write synchronously so re-loading from the store (on navigation or re-fetch)
        // always restores the correct lock state regardless of async save timing.
        lockedOverrides.set(page.id, (next as any).locked ?? false);
      }
      handleSave(next);
    }
  };

  return (
    <main className="content-area">
      <div className="content-toolbar">
        <input
          className="content-title"
          value={title}
          onChange={(e) => { setTitle(e.target.value); setDirty(true); }}
          onBlur={() => handleSave()}
        />
        <button
          className={`btn-save ${dirty ? 'btn-save--dirty' : ''}`}
          onClick={() => handleSave()}
          disabled={saving || !dirty}
        >
          {saving ? 'Saving…' : dirty ? 'Save' : 'Saved'}
        </button>
      </div>

      <div className="content-body">
        {content && page.type === 'Note' && (
          <NoteEditor content={content as NoteContent} onChange={(c) => handleContentChange(c)} />
        )}
        {content && page.type === 'Recipe' && (
          <RecipeEditor
            content={content as RecipeContent}
            onChange={(c) => handleContentChange(c)}
            categoryId={category?.id}
            pageTitle={title}
          />
        )}
        {content && page.type === 'Quote' && (
          <QuoteEditor content={content as QuoteContent} onChange={(c) => handleContentChange(c)} />
        )}
        {content && page.type === 'HomeInventory' && (
          <InventoryEditor
            content={content as HomeInventoryContent}
            onChange={(c) => handleContentChange(c)}
            categoryId={category?.id ?? ''}
            pageId={page.id}
            attachments={page.attachments}
          />
        )}
        {content && page.type === 'Password' && (
          <PasswordEditor content={content as PasswordContent} onChange={(c) => handleContentChange(c)} />
        )}
        {content && page.type === 'Reminder' && (
          <ReminderEditor content={content as ReminderContent} onChange={(c) => handleContentChange(c)} />
        )}
        {content && page.type === 'ShoppingList' && (
          <ShoppingListEditor content={content as ShoppingListContent} onChange={(c) => handleContentChange(c)} />
        )}
      </div>
    </main>
  );
};

export default ContentArea;
