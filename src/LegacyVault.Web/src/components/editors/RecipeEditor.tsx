import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import type { AppDispatch } from '../../store';
import { createPage } from '../../store/pagesSlice';
import type { RecipeContent, ShoppingListContent, ShoppingListItem } from '../../types';
import { shoppingListApi } from '../../api/shoppingListApi';
import type { ShoppingListRef } from '../../api/shoppingListApi';
import { pagesApi } from '../../api/pagesApi';
import './FormEditor.css';

interface Props {
  content: RecipeContent;
  onChange: (content: RecipeContent) => void;
  categoryId?: string;
  pageTitle?: string;
}

const RecipeEditor: React.FC<Props> = ({ content, onChange, categoryId, pageTitle }) => {
  const dispatch = useDispatch<AppDispatch>();
  const update = (patch: Partial<RecipeContent>) => onChange({ ...content, ...patch });
  const [checked, setChecked] = useState<Set<number>>(new Set());

  // Shopping list picker state
  const [pickerOpen, setPickerOpen] = useState(false);
  const [lists, setLists] = useState<ShoppingListRef[]>([]);
  const [pickerLoading, setPickerLoading] = useState(false);
  const [pickerStatus, setPickerStatus] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [createName, setCreateName] = useState('');

  const toggleCheck = (i: number) =>
    setChecked((prev) => { const s = new Set(prev); s.has(i) ? s.delete(i) : s.add(i); return s; });

  const updateListItem = (field: 'ingredients' | 'instructions', index: number, value: string) => {
    const list = [...content[field]];
    list[index] = value;
    update({ [field]: list });
  };

  const addListItem = (field: 'ingredients' | 'instructions') =>
    update({ [field]: [...content[field], ''] });

  const removeListItem = (field: 'ingredients' | 'instructions', index: number) => {
    const list = content[field].filter((_, i) => i !== index);
    update({ [field]: list });
  };

  const openPicker = async () => {
    setPickerOpen(true);
    setPickerStatus('');
    setShowCreate(false);
    setPickerLoading(true);
    try {
      const res = await shoppingListApi.getAll();
      setLists(res.data);
    } catch {
      setPickerStatus('Failed to load shopping lists.');
    } finally {
      setPickerLoading(false);
    }
  };

  const addToList = async (list: ShoppingListRef) => {
    if (list.isEncrypted) {
      setPickerStatus('Cannot add to an encrypted shopping list directly.');
      return;
    }
    setPickerStatus('Adding…');
    try {
      const res = await pagesApi.getById(list.categoryId, list.id);
      const slContent: ShoppingListContent = JSON.parse(res.data.content);
      const newItems: ShoppingListItem[] = content.ingredients
        .filter((i) => i.trim())
        .map((i) => ({ id: crypto.randomUUID(), name: i.trim(), quantity: '', checked: false }));
      const updated: ShoppingListContent = { ...slContent, items: [...slContent.items, ...newItems] };
      await pagesApi.update(list.categoryId, list.id, { content: JSON.stringify(updated) });
      setPickerStatus(`✓ Added ${newItems.length} ingredient${newItems.length !== 1 ? 's' : ''} to "${list.title}"`);
    } catch {
      setPickerStatus('Failed to add items.');
    }
  };

  const handleCreateList = async () => {
    if (!createName.trim() || !categoryId) return;
    setPickerStatus('Creating…');
    const items: ShoppingListItem[] = content.ingredients
      .filter((i) => i.trim())
      .map((i) => ({ id: crypto.randomUUID(), name: i.trim(), quantity: '', checked: false }));
    try {
      await dispatch(createPage({
        categoryId,
        title: createName.trim(),
        type: 'ShoppingList',
        content: JSON.stringify({ items, notes: '' } as ShoppingListContent),
        isEncrypted: false,
      }));
      setPickerStatus(`✓ Created "${createName.trim()}" with ${items.length} ingredient${items.length !== 1 ? 's' : ''}`);
      setShowCreate(false);
      setCreateName('');
    } catch {
      setPickerStatus('Failed to create list.');
    }
  };

  if (content.locked) {
    return (
      <div className="recipe-card">
        <button
          className="recipe-lock-toggle"
          onClick={() => { setChecked(new Set()); update({ locked: false }); }}
          title="Unlock to edit"
        >
          🔒 Locked
        </button>

        {(content.servings > 0 || content.prepTime || content.cookTime) && (
          <div className="recipe-card-meta">
            {content.servings > 0 && (
              <span className="recipe-meta-badge">🍽️ {content.servings} serving{content.servings !== 1 ? 's' : ''}</span>
            )}
            {content.prepTime && (
              <span className="recipe-meta-badge">⏱️ Prep {content.prepTime}</span>
            )}
            {content.cookTime && (
              <span className="recipe-meta-badge">🔥 Cook {content.cookTime}</span>
            )}
          </div>
        )}

        <div className="recipe-card-columns">
          {content.ingredients.length > 0 && (
            <div className="recipe-card-section">
              <h3 className="recipe-card-heading">Ingredients</h3>
              <ul className="recipe-ingredient-list">
                {content.ingredients.map((item, i) => (
                  <li
                    key={i}
                    className={`recipe-ingredient-item${checked.has(i) ? ' recipe-ingredient-item--checked' : ''}`}
                    onClick={() => toggleCheck(i)}
                  >
                    <span className="recipe-ingredient-check">{checked.has(i) ? '✓' : ''}</span>
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          {content.instructions.length > 0 && (
            <div className="recipe-card-section recipe-card-section--instructions">
              <h3 className="recipe-card-heading">Instructions</h3>
              <ol className="recipe-step-list">
                {content.instructions.map((step, i) => (
                  <li key={i} className="recipe-step-item">
                    <span className="recipe-step-num">{i + 1}</span>
                    <span>{step}</span>
                  </li>
                ))}
              </ol>
            </div>
          )}
        </div>

        {content.notes && (
          <div className="recipe-card-notes">
            <h3 className="recipe-card-heading">Notes</h3>
            <p>{content.notes}</p>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="form-editor">
      <div className="form-editor-header">
        <button
          className="editor-lock-btn"
          onClick={() => update({ locked: true })}
          title="Lock recipe into card view"
        >
          🔓 Lock Recipe
        </button>
        {content.ingredients.some((i) => i.trim()) && (
          <button
            className="editor-lock-btn"
            onClick={openPicker}
            title="Add ingredients to a shopping list"
          >
            🛒 Shopping List
          </button>
        )}
      </div>

      <div className="form-row form-row--inline">
        <div className="form-field">
          <label>Servings</label>
          <input type="number" value={content.servings} min={1}
            onChange={(e) => update({ servings: parseInt(e.target.value) || 1 })} />
        </div>
        <div className="form-field">
          <label>Prep Time</label>
          <input placeholder="e.g. 20 min" value={content.prepTime}
            onChange={(e) => update({ prepTime: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Cook Time</label>
          <input placeholder="e.g. 45 min" value={content.cookTime}
            onChange={(e) => update({ cookTime: e.target.value })} />
        </div>
      </div>

      <div className="form-section">
        <div className="form-section-header">
          <h3>Ingredients</h3>
          <button className="btn-add-item" onClick={() => addListItem('ingredients')}>+ Add</button>
        </div>
        {content.ingredients.map((item, i) => (
          <div key={i} className="list-item">
            <span className="list-bullet">•</span>
            <input value={item} onChange={(e) => updateListItem('ingredients', i, e.target.value)}
              placeholder="Ingredient..." />
            <button className="btn-remove" onClick={() => removeListItem('ingredients', i)}>✕</button>
          </div>
        ))}
        {content.ingredients.length === 0 && (
          <p className="form-empty">No ingredients yet. Click + Add.</p>
        )}
      </div>

      <div className="form-section">
        <div className="form-section-header">
          <h3>Instructions</h3>
          <button className="btn-add-item" onClick={() => addListItem('instructions')}>+ Add Step</button>
        </div>
        {content.instructions.map((step, i) => (
          <div key={i} className="list-item list-item--numbered">
            <span className="list-number">{i + 1}.</span>
            <textarea value={step} rows={2}
              onChange={(e) => updateListItem('instructions', i, e.target.value)}
              placeholder={`Step ${i + 1}...`} />
            <button className="btn-remove" onClick={() => removeListItem('instructions', i)}>✕</button>
          </div>
        ))}
      </div>

      <div className="form-field">
        <label>Notes</label>
        <textarea value={content.notes} rows={3} placeholder="Additional notes..."
          onChange={(e) => update({ notes: e.target.value })} />
      </div>

      {/* Shopping list picker modal */}
      {pickerOpen && (
        <div className="sl-picker-overlay" onClick={() => setPickerOpen(false)}>
          <div className="sl-picker-modal" onClick={(e) => e.stopPropagation()}>
            <div className="sl-picker-header">
              <span>Add ingredients to shopping list</span>
              <button className="sl-picker-close" onClick={() => setPickerOpen(false)}>✕</button>
            </div>
            <div className="sl-picker-body">
              {pickerStatus && (
                <p className={`sl-picker-status${pickerStatus.startsWith('✓') ? ' sl-picker-status--ok' : pickerStatus.includes('Failed') || pickerStatus.includes('Cannot') ? ' sl-picker-status--err' : ''}`}>
                  {pickerStatus}
                </p>
              )}
              {pickerLoading ? (
                <p className="sl-picker-status">Loading lists…</p>
              ) : (
                <>
                  {lists.length > 0 && (
                    <>
                      <p className="sl-picker-section-label">Add to existing list</p>
                      <div>
                        {lists.map((list) => (
                          <button key={list.id} className="sl-picker-list-item" onClick={() => addToList(list)}>
                            <span className="sl-picker-list-title">🛒 {list.title}</span>
                            {list.isEncrypted && <span className="sl-picker-list-lock">🔒</span>}
                          </button>
                        ))}
                      </div>
                      <div className="sl-picker-divider" />
                    </>
                  )}
                  {!showCreate ? (
                    <button
                      className="sl-picker-add-btn"
                      onClick={() => {
                        setShowCreate(true);
                        setCreateName(pageTitle ? `${pageTitle} Shopping List` : '');
                      }}
                    >
                      + Create new shopping list
                    </button>
                  ) : (
                    <div className="sl-picker-create-form">
                      <label className="sl-picker-create-label">New list name</label>
                      <input
                        className="sl-picker-create-input"
                        value={createName}
                        autoFocus
                        onChange={(e) => setCreateName(e.target.value)}
                        onKeyDown={(e) => { if (e.key === 'Enter') handleCreateList(); }}
                      />
                      <div className="sl-picker-create-actions">
                        <button
                          className="sl-add-btn"
                          onClick={handleCreateList}
                          disabled={!createName.trim() || !categoryId}
                        >
                          Create & Add
                        </button>
                        <button className="sl-picker-close" onClick={() => setShowCreate(false)}>Cancel</button>
                      </div>
                    </div>
                  )}
                  {lists.length === 0 && !showCreate && !pickerStatus && (
                    <p className="sl-picker-empty">No shopping lists yet.</p>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RecipeEditor;
