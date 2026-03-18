import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../../store';
import { selectCategory, createCategory, deleteCategory, renameCategory, toggleCategoryFavorite, lockCategory } from '../../store/categoriesSlice';
import { cryptoService } from '../../crypto/cryptoService';
import { keyCache } from '../../crypto/keyCache';
import type { Category, CategoryType } from '../../types';
import './Sidebar.css';

interface Props {
  collapsed: boolean;
  onToggle: () => void;
}

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
  cat: Category;
  x: number;
  y: number;
}

const Sidebar: React.FC<Props> = ({ collapsed, onToggle }) => {
  const dispatch = useDispatch<AppDispatch>();
  const { items, selectedId } = useSelector((s: RootState) => s.categories);
  const [showNew, setShowNew] = useState(false);
  const [newName, setNewName] = useState('');
  const [newIcon, setNewIcon] = useState('📁');
  const [newType, setNewType] = useState<CategoryType>('General');
  const [newEncrypted, setNewEncrypted] = useState(false);
  const [newPassword, setNewPassword] = useState('');
  const [newPasswordConfirm, setNewPasswordConfirm] = useState('');
  const [newPasswordHint, setNewPasswordHint] = useState('');
  const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);
  const contextMenuRef = useRef<HTMLDivElement>(null);
  const [renameId, setRenameId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');

  const isVault = newType === 'Vault';

  useEffect(() => {
    if (!contextMenu) return;
    const onMouseDown = (e: MouseEvent) => {
      if (contextMenuRef.current && !contextMenuRef.current.contains(e.target as Node))
        setContextMenu(null);
    };
    const onKeyDown = (e: KeyboardEvent) => { if (e.key === 'Escape') setContextMenu(null); };
    document.addEventListener('mousedown', onMouseDown);
    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('mousedown', onMouseDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [contextMenu]);

  const needsEncryption = isVault || newEncrypted;
  const passwordMismatch = needsEncryption && newPasswordConfirm.length > 0 && newPassword !== newPasswordConfirm;
  const passwordIncomplete = needsEncryption && (!newPassword || !newPasswordConfirm || newPassword !== newPasswordConfirm);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    if (needsEncryption && !newPassword) return;
    if (passwordIncomplete) return;

    let encryptionSalt: string | undefined;

    if (needsEncryption && newPassword) {
      encryptionSalt = cryptoService.generateSalt();
      const key = await cryptoService.deriveKey(newPassword, encryptionSalt);
      const result = await dispatch(createCategory({
        name: newName,
        icon: isVault ? '🔑' : newIcon,
        type: newType,
        isEncrypted: true,
        encryptionSalt,
        passwordHint: newPasswordHint.trim() || undefined,
      }));
      if (createCategory.fulfilled.match(result)) {
        keyCache.set(result.payload.id, key);
      }
    } else {
      await dispatch(createCategory({ name: newName, icon: newIcon, type: newType, isEncrypted: false }));
    }

    setShowNew(false);
    setNewName('');
    setNewIcon('📁');
    setNewType('General');
    setNewEncrypted(false);
    setNewPassword('');
    setNewPasswordConfirm('');
    setNewPasswordHint('');
  };

  const openContextMenu = (e: React.MouseEvent, cat: Category) => {
    e.preventDefault();
    e.stopPropagation();
    setContextMenu({ cat, x: e.clientX, y: e.clientY });
  };

  const openContextMenuFromBtn = (e: React.MouseEvent, cat: Category) => {
    e.stopPropagation();
    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
    setContextMenu({ cat, x: rect.left, y: rect.bottom + 4 });
  };

  const handleRenameClick = (cat: Category) => {
    setContextMenu(null);
    setRenameId(cat.id);
    setRenameValue(cat.name);
  };

  const handleRenameCommit = async (cat: Category) => {
    const trimmed = renameValue.trim();
    setRenameId(null);
    if (!trimmed || trimmed === cat.name) return;
    dispatch(renameCategory({ id: cat.id, name: trimmed }));
  };

  const handleRenameCancel = () => setRenameId(null);

  const handleDelete = (cat: Category) => {
    setContextMenu(null);
    const warning = cat.type === 'Vault'
      ? `Delete safe vault "${cat.name}"?\n\nThis will permanently delete all password entries inside it. This cannot be undone.`
      : `Delete vault "${cat.name}"?\n\nThis will permanently delete all pages inside it. This cannot be undone.`;
    if (!confirm(warning)) return;
    keyCache.clear(cat.id);
    dispatch(deleteCategory(cat.id));
  };

  const handleTypeChange = (t: CategoryType) => {
    setNewType(t);
    if (t === 'Vault') setNewEncrypted(true);
    setNewPassword('');
    setNewPasswordConfirm('');
    setNewPasswordHint('');
  };

  return (
    <aside className={`sidebar${collapsed ? ' sidebar--collapsed' : ''}`}>
      <div className="sidebar-panel-header">
        {!collapsed && <span className="sidebar-section-label">Vaults</span>}
        <button className="panel-toggle-btn" onClick={onToggle} title={collapsed ? 'Expand vault shelf' : 'Collapse vault shelf'}>
          {collapsed ? <ExpandIcon /> : <CollapseIcon />}
        </button>
      </div>

      {collapsed && (
        <div className="panel-collapsed-label-wrap" onClick={onToggle} title="Expand vault shelf">
          <span className="panel-collapsed-label">Vaults</span>
        </div>
      )}

      {!collapsed && (
        <>
          <nav className="sidebar-nav">
            {[...items]
              .sort((a, b) => {
                if (a.isFavorite !== b.isFavorite) return a.isFavorite ? -1 : 1;
                return a.name.localeCompare(b.name);
              })
              .map((cat: Category) => (
              <button
                key={cat.id}
                className={`sidebar-item ${selectedId === cat.id ? 'active' : ''} ${cat.type === 'Vault' ? 'sidebar-item--vault' : ''}`}
                onClick={() => dispatch(selectCategory(cat.id))}
                onContextMenu={(e) => openContextMenu(e, cat)}
              >
                <span className="sidebar-icon">{cat.icon}</span>

                {renameId === cat.id ? (
                  <input
                    className="sidebar-rename-input"
                    value={renameValue}
                    autoFocus
                    onClick={(e) => e.stopPropagation()}
                    onChange={(e) => setRenameValue(e.target.value)}
                    onBlur={() => handleRenameCommit(cat)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') { e.preventDefault(); handleRenameCommit(cat); }
                      if (e.key === 'Escape') { e.preventDefault(); handleRenameCancel(); }
                    }}
                  />
                ) : (
                  <span className="sidebar-name">{cat.name}</span>
                )}

                <span
                  className={`sidebar-fav-btn${cat.isFavorite ? ' sidebar-fav-btn--on' : ''}`}
                  role="button"
                  title={cat.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                  onClick={(e) => { e.stopPropagation(); dispatch(toggleCategoryFavorite(cat.id)); }}
                >
                  ★
                </span>
                {cat.isEncrypted && (
                  <span
                    className="sidebar-lock sidebar-lock--btn"
                    role="button"
                    title="Lock shelf"
                    onClick={(e) => { e.stopPropagation(); keyCache.clear(cat.id); dispatch(lockCategory(cat.id)); }}
                  >
                    🔒
                  </span>
                )}
                <span className="sidebar-count">{cat.pageCount}</span>
                <span
                  className="sidebar-more-btn"
                  role="button"
                  title="More options"
                  onClick={(e) => openContextMenuFromBtn(e, cat)}
                >
                  ⋮
                </span>
              </button>
            ))}
          </nav>

          {showNew ? (
            <div className="sidebar-new-form">
              <div className="sidebar-type-toggle">
                <button
                  className={`sidebar-type-btn ${newType === 'General' ? 'active' : ''}`}
                  onClick={() => handleTypeChange('General')}
                >
                  📁 Vault
                </button>
                <button
                  className={`sidebar-type-btn ${newType === 'Vault' ? 'active' : ''}`}
                  onClick={() => handleTypeChange('Vault')}
                >
                  🔑 Safe
                </button>
              </div>

              <input placeholder="Name" value={newName} onChange={(e) => setNewName(e.target.value)} autoFocus />

              {!isVault && (
                <input placeholder="Icon (emoji)" value={newIcon} onChange={(e) => setNewIcon(e.target.value)} maxLength={4} />
              )}

              {!isVault && (
                <label className="sidebar-checkbox">
                  <input type="checkbox" checked={newEncrypted} onChange={(e) => setNewEncrypted(e.target.checked)} />
                  Encrypt
                </label>
              )}

              {isVault && (
                <p className="sidebar-vault-hint">🔒 Safe vaults are always encrypted. All entries will be stored securely.</p>
              )}

              {(isVault || newEncrypted) && (
                <>
                  <input
                    type="password"
                    placeholder={isVault ? 'Vault master password' : 'Encryption password'}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                  />
                  <input
                    type="password"
                    placeholder="Confirm password"
                    value={newPasswordConfirm}
                    onChange={(e) => setNewPasswordConfirm(e.target.value)}
                    className={passwordMismatch ? 'sidebar-input--error' : ''}
                  />
                  {passwordMismatch && (
                    <p className="sidebar-password-error">Passwords do not match</p>
                  )}
                  <input
                    type="text"
                    placeholder="Password hint (optional)"
                    value={newPasswordHint}
                    onChange={(e) => setNewPasswordHint(e.target.value)}
                    maxLength={200}
                  />
                  <p className="sidebar-password-error" style={{ color: 'var(--color-text-muted)', fontStyle: 'italic' }}>
                    ⚠️ If you forget this password, your data cannot be recovered.
                  </p>
                </>
              )}

              <div className="sidebar-new-actions">
                <button className="btn-primary" onClick={handleCreate} disabled={passwordIncomplete}>Create</button>
                <button className="btn-ghost" onClick={() => setShowNew(false)}>Cancel</button>
              </div>
            </div>
          ) : (
            <button className="sidebar-add" onClick={() => setShowNew(true)}>+ New Vault</button>
          )}
        </>
      )}

      {contextMenu && (
        <div
          ref={contextMenuRef}
          className="sidebar-context-menu"
          style={{ top: contextMenu.y, left: contextMenu.x }}
        >
          <button className="sidebar-context-item" onClick={() => handleRenameClick(contextMenu.cat)}>
            Rename vault
          </button>
          <div className="sidebar-context-divider" />
          <button className="sidebar-context-item sidebar-context-item--danger" onClick={() => handleDelete(contextMenu.cat)}>
            Delete vault
          </button>
        </div>
      )}
    </aside>
  );
};

export default Sidebar;
