import React, { useState, useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { selectPage, createPage, deletePage, movePage, savePage, togglePageFavorite } from '../../store/pagesSlice';
import { unlockCategory } from '../../store/categoriesSlice';
import { cryptoService } from '../../crypto/cryptoService';
import { keyCache } from '../../crypto/keyCache';
import type { Page, PageType } from '../../types';
import EncryptionPrompt from '../auth/EncryptionPrompt';
import { filterPages, matchingTags } from '../../utils/searchPages';
import './PageList.css';

interface Props {
  collapsed: boolean;
  onToggle: () => void;
}

const PAGE_TYPE_ICONS: Record<PageType, string> = {
  Recipe: '🍽️',
  Quote: '💬',
  Note: '📝',
  HomeInventory: '🏠',
  Password: '🔑',
  Reminder: '🔔',
  ShoppingList: '🛒',
};

const CollapseIcon = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <polyline points="9,2 4,7 9,12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    <polyline points="13,2 8,7 13,12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

const ExpandIcon = () => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
    <polyline points="5,2 10,7 5,12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
    <polyline points="1,2 6,7 1,12" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

interface ContextMenuState {
  page: Page;
  x: number;
  y: number;
}

const PageList: React.FC<Props> = ({ collapsed, onToggle }) => {
  const dispatch = useDispatch<AppDispatch>();
  const { items, selectedId, status } = useSelector((s: RootState) => s.pages);
  const category = useSelector((s: RootState) =>
    s.categories.items.find((c) => c.id === s.categories.selectedId)
  );
  const allCategories = useSelector((s: RootState) => s.categories.items);

  const [showNew, setShowNew] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newType, setNewType] = useState<PageType>('Note');
  const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);
  const [moveTarget, setMoveTarget] = useState<Page | null>(null);
  const contextMenuRef = useRef<HTMLDivElement>(null);
  const unlockedCategoryIds = useSelector((s: RootState) => s.categories.unlockedCategoryIds);
  const vaultUnlocked = Boolean(category?.id && unlockedCategoryIds.includes(category.id));
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'date' | 'type'>('date');
  const [renameId, setRenameId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');

  const lockTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const prevCategoryRef = useRef(category);

  // Auto-lock vault 5 min after navigating away; reset form/search on category change
  useEffect(() => {
    const prev = prevCategoryRef.current;
    prevCategoryRef.current = category;

    // Left an unlocked encrypted shelf → start 5-min lock timer
    if (prev?.isEncrypted && prev.id && keyCache.has(prev.id) && prev.id !== category?.id) {
      const vaultId = prev.id;
      if (lockTimerRef.current) clearTimeout(lockTimerRef.current);
      lockTimerRef.current = setTimeout(() => {
        keyCache.clear(vaultId);
        lockTimerRef.current = null;
      }, 5 * 60 * 1000);
    }

    // Returned to encrypted shelf before timer fired → cancel lock
    if (category?.isEncrypted && lockTimerRef.current) {
      clearTimeout(lockTimerRef.current);
      lockTimerRef.current = null;
    }

    if (!category) return;
    setShowNew(false);
    setSearchQuery('');
  }, [category?.id]);

  // Clean up timer on unmount
  useEffect(() => () => { if (lockTimerRef.current) clearTimeout(lockTimerRef.current); }, []);

  useEffect(() => {
    if (!contextMenu) return;
    const handleMouseDown = (e: MouseEvent) => {
      if (contextMenuRef.current && !contextMenuRef.current.contains(e.target as Node)) {
        setContextMenu(null);
      }
    };
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setContextMenu(null);
    };
    document.addEventListener('mousedown', handleMouseDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handleMouseDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [contextMenu]);

  const isVault = category?.type === 'Vault';
  const effectiveType = isVault ? 'Password' : newType;
  const filteredItems = filterPages(items, searchQuery).slice().sort((a, b) => {
    if (a.isFavorite !== b.isFavorite) return a.isFavorite ? -1 : 1;
    if (sortBy === 'name') return a.title.localeCompare(b.title);
    if (sortBy === 'type') return a.type.localeCompare(b.type) || a.title.localeCompare(b.title);
    return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
  });

  const handleVaultUnlock = async (password: string) => {
    if (!category?.encryptionSalt) throw new Error('Missing encryption salt');
    const key = await cryptoService.deriveKey(password, category.encryptionSalt);
    // Verify against the first encrypted entry — AES-GCM auth tag will reject a wrong key
    const testPage = items.find((p) => p.isEncrypted && p.encryptionIV);
    if (testPage) {
      await cryptoService.decrypt(testPage.content, testPage.encryptionIV!, key);
    }
    keyCache.set(category.id, key);
    dispatch(unlockCategory(category.id));
  };

  const handleCreate = async () => {
    if (!newTitle.trim() || !category) return;

    let content = getDefaultContent(effectiveType);
    let encryptionIV: string | undefined;

    if (category.isEncrypted) {
      const key = keyCache.get(category.id);
      if (!key) return; // vault is locked — shouldn't be reachable but guard anyway
      const encrypted = await cryptoService.encrypt(content, key);
      content = encrypted.ciphertext;
      encryptionIV = encrypted.iv;
    }

    await dispatch(createPage({
      categoryId: category.id,
      title: newTitle,
      type: effectiveType,
      content,
      isEncrypted: category.isEncrypted,
      encryptionIV,
    }));
    setShowNew(false);
    setNewTitle('');
  };

  const openContextMenu = (e: React.MouseEvent, page: Page) => {
    e.preventDefault();
    e.stopPropagation();
    setContextMenu({ page, x: e.clientX, y: e.clientY });
  };

  const openContextMenuFromButton = (e: React.MouseEvent, page: Page) => {
    e.stopPropagation();
    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
    setContextMenu({ page, x: rect.left, y: rect.bottom + 4 });
  };

  const handleMoveClick = (page: Page) => {
    setContextMenu(null);
    setMoveTarget(page);
  };

  const handleMove = async (targetCategoryId: string) => {
    if (!moveTarget) return;
    await dispatch(movePage({ categoryId: moveTarget.categoryId, id: moveTarget.id, targetCategoryId }));
    setMoveTarget(null);
  };

  const handleDelete = (page: Page) => {
    setContextMenu(null);
    if (!confirm(`Delete "${page.title}"?\n\nThis action cannot be undone.`)) return;
    dispatch(deletePage({ categoryId: page.categoryId, id: page.id }));
  };

  const handleRenameClick = (page: Page) => {
    setContextMenu(null);
    setRenameId(page.id);
    setRenameValue(page.title);
  };

  const handleRenameCommit = (page: Page) => {
    const trimmed = renameValue.trim();
    setRenameId(null);
    if (!trimmed || trimmed === page.title) return;
    dispatch(savePage({ categoryId: page.categoryId, id: page.id, title: trimmed }));
  };

  const handleRenameCancel = () => setRenameId(null);

  const otherCategories = moveTarget
    ? allCategories.filter((c) => c.id !== moveTarget.categoryId)
    : [];

  return (
    <div className={`page-list${collapsed ? ' page-list--collapsed' : ''}`}>
      <div className="page-panel-header">
        {!collapsed && (
          <span className="page-list-title">
            {category ? `${category.icon} ${category.name}` : 'Page Shelf'}
          </span>
        )}
        <button className="panel-toggle-btn" onClick={onToggle} title={collapsed ? 'Expand page shelf' : 'Collapse page shelf'}>
          {collapsed ? <ExpandIcon /> : <CollapseIcon />}
        </button>
      </div>

      {collapsed && (
        <div className="panel-collapsed-label-wrap" onClick={onToggle} title="Expand page shelf">
          <span className="panel-collapsed-label">{category?.name ?? 'Pages'}</span>
        </div>
      )}

      {!collapsed && (
        <>
          {!category ? (
            <div className="page-list-empty">
              <p>Select a vault to get started</p>
            </div>
          ) : category.isEncrypted && !vaultUnlocked ? (
            <div className="vault-lock-screen">
              <EncryptionPrompt categoryName={category.name} passwordHint={category.passwordHint} onUnlock={handleVaultUnlock} />
            </div>
          ) : (
            <>
              {status === 'loading' && <div className="page-list-loading">Loading...</div>}

              {items.length > 0 && (
                <div className="page-search">
                  <input
                    className="page-search-input"
                    type="search"
                    placeholder="Search pages…"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                  {searchQuery && (
                    <button className="page-search-clear" onClick={() => setSearchQuery('')} title="Clear search">✕</button>
                  )}
                </div>
              )}

              {items.length > 1 && (
                <div className="page-sort-bar">
                  <span className="page-sort-label">Sort:</span>
                  {(['date', 'name', 'type'] as const).map((opt) => (
                    <button
                      key={opt}
                      className={`page-sort-btn${sortBy === opt ? ' active' : ''}`}
                      onClick={() => setSortBy(opt)}
                    >
                      {opt === 'date' ? 'Date' : opt === 'name' ? 'Name' : 'Type'}
                    </button>
                  ))}
                </div>
              )}

              <div className="page-list-items">
                {filteredItems.length === 0 && searchQuery ? (
                  <div className="page-list-empty page-list-empty--search">
                    No results for <em>"{searchQuery}"</em>
                  </div>
                ) : filteredItems.map((page) => {
                  const hitTags = matchingTags(page, searchQuery);
                  return (
                    <button
                      key={page.id}
                      className={`page-item ${selectedId === page.id ? 'active' : ''}`}
                      onClick={() => dispatch(selectPage(page.id))}
                      onContextMenu={(e) => openContextMenu(e, page)}
                    >
                      <span className="page-type-icon">{PAGE_TYPE_ICONS[page.type]}</span>
                      <div className="page-item-info">
                        {renameId === page.id ? (
                          <input
                            className="page-rename-input"
                            value={renameValue}
                            autoFocus
                            onClick={(e) => e.stopPropagation()}
                            onChange={(e) => setRenameValue(e.target.value)}
                            onBlur={() => handleRenameCommit(page)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter') { e.preventDefault(); handleRenameCommit(page); }
                              if (e.key === 'Escape') { e.preventDefault(); handleRenameCancel(); }
                            }}
                          />
                        ) : (
                          <span className="page-item-title">{page.title}</span>
                        )}
                        {hitTags.length > 0 ? (
                          <span className="page-item-meta">
                            {hitTags.map((t) => (
                              <span key={t} className="page-item-tag">{t}</span>
                            ))}
                          </span>
                        ) : (
                          <span className="page-item-meta">
                            {page.type} · {new Date(page.updatedAt).toLocaleDateString()}
                          </span>
                        )}
                      </div>
                      <span
                        className={`page-fav-btn${page.isFavorite ? ' page-fav-btn--on' : ''}`}
                        role="button"
                        title={page.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                        onClick={(e) => { e.stopPropagation(); dispatch(togglePageFavorite({ categoryId: page.categoryId, id: page.id })); }}
                      >
                        ★
                      </span>
                      {page.isEncrypted && <span className="page-lock">🔒</span>}
                      <span
                        className="page-menu-btn"
                        role="button"
                        onClick={(e) => openContextMenuFromButton(e, page)}
                        title="More options"
                      >
                        ⋮
                      </span>
                    </button>
                  );
                })}
              </div>

              {showNew ? (
                <div className="page-new-form">
                  <input placeholder={isVault ? 'Entry title (e.g. Gmail)' : 'Page title'} value={newTitle}
                    onChange={(e) => setNewTitle(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleCreate()} autoFocus />
                  {!isVault && (
                    <select value={newType} onChange={(e) => setNewType(e.target.value as PageType)}>
                      <option value="Note">📝 Note</option>
                      <option value="Reminder">🔔 Reminder</option>
                      <option value="Recipe">🍽️ Recipe</option>
                      <option value="Quote">💬 Quote</option>
                      <option value="HomeInventory">🏠 Home Inventory</option>
                      <option value="ShoppingList">🛒 Shopping List</option>
                    </select>
                  )}
                  <div className="page-new-actions">
                    <button className="btn-primary" onClick={handleCreate}>Add</button>
                    <button className="btn-ghost" onClick={() => setShowNew(false)}>Cancel</button>
                  </div>
                </div>
              ) : (
                <button className="page-add" onClick={() => setShowNew(true)}>
                  {isVault ? '+ New Entry' : '+ New Page'}
                </button>
              )}
            </>
          )}
        </>
      )}

      {/* Context menu */}
      {contextMenu && (
        <div
          ref={contextMenuRef}
          className="context-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
        >
          <button className="context-menu-item" onClick={() => handleRenameClick(contextMenu.page)}>
            Rename page
          </button>
          <button className="context-menu-item" onClick={() => handleMoveClick(contextMenu.page)}>
            Move to category…
          </button>
          <div className="context-menu-divider" />
          <button className="context-menu-item context-menu-item--danger" onClick={() => handleDelete(contextMenu.page)}>
            Delete page
          </button>
        </div>
      )}

      {/* Move dialog */}
      {moveTarget && (
        <div className="move-modal-overlay" onMouseDown={() => setMoveTarget(null)}>
          <div className="move-modal" onMouseDown={(e) => e.stopPropagation()}>
            <div className="move-modal-header">
              <span>Move "{moveTarget.title}" to…</span>
              <button className="move-modal-close" onClick={() => setMoveTarget(null)}>✕</button>
            </div>
            {otherCategories.length === 0 ? (
              <p className="move-modal-empty">No other categories available.</p>
            ) : (
              <div className="move-modal-list">
                {otherCategories.map((cat) => (
                  <button key={cat.id} className="move-modal-item" onClick={() => handleMove(cat.id)}>
                    <span>{cat.icon}</span>
                    <span>{cat.name}</span>
                    {cat.isEncrypted && <span className="move-modal-lock">🔒</span>}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

function getDefaultContent(type: PageType): string {
  const defaults: Record<PageType, object> = {
    Note: { body: '' },
    Recipe: { ingredients: [], instructions: [], servings: 4, prepTime: '', cookTime: '', notes: '' },
    Quote: { text: '', author: '', source: '', tags: [] },
    HomeInventory: { itemName: '', description: '', location: '', value: 0, purchaseDate: '', serialNumber: '', warrantyExpiry: '', attachmentIds: [] },
    Password: { url: '', username: '', password: '', notes: '', totp: '' },
    Reminder: { date: '', tag: 'custom', recurrence: 'yearly', notes: '', notifyEnabled: false, notifyBefore: 1, notifyUnit: 'days' },
    ShoppingList: { items: [], notes: '' },
  };
  return JSON.stringify(defaults[type]);
}

export default PageList;
