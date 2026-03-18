import React, { useState } from 'react';
import type { ShoppingListContent, ShoppingListItem } from '../../types';
import './FormEditor.css';

interface Props {
  content: ShoppingListContent;
  onChange: (content: ShoppingListContent) => void;
}

const ShoppingListEditor: React.FC<Props> = ({ content, onChange }) => {
  const [newItem, setNewItem] = useState('');
  const [newQty, setNewQty] = useState('');
  const [showQtyInput, setShowQtyInput] = useState(false);

  const update = (patch: Partial<ShoppingListContent>) => onChange({ ...content, ...patch });

  const addItem = () => {
    const name = newItem.trim();
    if (!name) return;
    const item: ShoppingListItem = {
      id: crypto.randomUUID(),
      name,
      quantity: newQty.trim(),
      checked: false,
    };
    update({ items: [...content.items, item] });
    setNewItem('');
    setNewQty('');
  };

  const toggleCheck = (id: string) => {
    update({
      items: content.items.map((item) =>
        item.id === id ? { ...item, checked: !item.checked } : item
      ),
    });
  };

  const removeItem = (id: string) => {
    update({ items: content.items.filter((item) => item.id !== id) });
  };

  const clearChecked = () => {
    update({ items: content.items.filter((item) => !item.checked) });
  };

  const clearAll = () => {
    if (!confirm('Delete all items from this list?')) return;
    update({ items: [] });
  };

  const unchecked = content.items.filter((i) => !i.checked);
  const checked = content.items.filter((i) => i.checked);
  const checkedCount = checked.length;

  return (
    <div className="form-editor shopping-list-editor">
      {/* Add item row */}
      <div className="sl-add-row">
        <input
          className="sl-add-input"
          value={newItem}
          placeholder="Add item…"
          onChange={(e) => setNewItem(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addItem(); } }}
        />
        <button
          type="button"
          className="sl-qty-toggle"
          title={showQtyInput ? 'Hide quantity' : 'Add quantity'}
          onClick={() => setShowQtyInput((v) => !v)}
        >
          #
        </button>
        {showQtyInput && (
          <input
            className="sl-qty-input"
            value={newQty}
            placeholder="Qty"
            onChange={(e) => setNewQty(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addItem(); } }}
          />
        )}
        <button className="sl-add-btn" onClick={addItem} disabled={!newItem.trim()}>
          + Add
        </button>
      </div>

      {/* Unchecked items */}
      {unchecked.length > 0 && (
        <ul className="sl-list">
          {unchecked.map((item) => (
            <li key={item.id} className="sl-item">
              <button className="sl-check" onClick={() => toggleCheck(item.id)} title="Mark as got" />
              <span className="sl-item-name">{item.name}</span>
              {item.quantity && <span className="sl-item-qty">{item.quantity}</span>}
              <button className="sl-remove" onClick={() => removeItem(item.id)} title="Remove">✕</button>
            </li>
          ))}
        </ul>
      )}

      {/* Checked items */}
      {checkedCount > 0 && (
        <div className="sl-checked-section">
          <div className="sl-checked-header">
            <span className="sl-checked-label">Got ({checkedCount})</span>
            <button className="sl-clear-btn" onClick={clearChecked}>Clear got items</button>
          </div>
          <ul className="sl-list sl-list--checked">
            {checked.map((item) => (
              <li key={item.id} className="sl-item sl-item--checked">
                <button className="sl-check sl-check--checked" onClick={() => toggleCheck(item.id)} title="Unmark" />
                <span className="sl-item-name">{item.name}</span>
                {item.quantity && <span className="sl-item-qty">{item.quantity}</span>}
                <button className="sl-remove" onClick={() => removeItem(item.id)} title="Remove">✕</button>
              </li>
            ))}
          </ul>
        </div>
      )}

      {content.items.length === 0 && (
        <p className="form-empty">No items yet. Type above and press Enter or + Add.</p>
      )}

      {content.items.length > 0 && (
        <div className="sl-footer">
          <button className="sl-clear-btn sl-clear-btn--danger" onClick={clearAll}>
            Delete all items
          </button>
        </div>
      )}

      <div className="form-field">
        <label>Notes</label>
        <textarea
          value={content.notes}
          rows={2}
          placeholder="e.g. Store, brand preferences…"
          onChange={(e) => update({ notes: e.target.value })}
        />
      </div>
    </div>
  );
};

export default ShoppingListEditor;
