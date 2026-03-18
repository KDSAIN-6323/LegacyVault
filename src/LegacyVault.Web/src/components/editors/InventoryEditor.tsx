import React, { useRef, useState } from 'react';
import type { AxiosError } from 'axios';
import { HomeInventoryContent, Attachment } from '../../types';
import { attachmentsApi } from '../../api/attachmentsApi';
import './FormEditor.css';

interface Props {
  content: HomeInventoryContent;
  onChange: (content: HomeInventoryContent) => void;
  categoryId: string;
  pageId: string;
  attachments: Attachment[];
}

const InventoryEditor: React.FC<Props> = ({ content, onChange, categoryId, pageId, attachments }) => {
  const update = (patch: Partial<HomeInventoryContent>) => onChange({ ...content, ...patch });
  const fileInputRef = useRef<HTMLInputElement>(null);
  // Local copy so newly uploaded attachments appear immediately without a server refetch
  const [localAttachments, setLocalAttachments] = useState<Attachment[]>(attachments);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (images.length >= 2) { e.target.value = ''; return; }
    try {
      const { data } = await attachmentsApi.upload(categoryId, pageId, file);
      setLocalAttachments((prev) => [...prev, data]);
      update({ attachmentIds: [...content.attachmentIds, data.id] });
    } catch (err) {
      const msg = (err as AxiosError<string>).response?.data || 'Upload failed.';
      alert(`Upload failed: ${msg}`);
    }
    e.target.value = '';
  };

  const handleImageDelete = async (att: Attachment) => {
    if (!confirm(`Delete "${att.fileName}"?`)) return;
    try {
      await attachmentsApi.delete(att.id);
    } catch {
      // File may already be gone on the server; remove from content regardless
    }
    setLocalAttachments((prev) => prev.filter((a) => a.id !== att.id));
    update({ attachmentIds: content.attachmentIds.filter((id) => id !== att.id) });
  };

  const images = localAttachments.filter((a) => content.attachmentIds.includes(a.id));

  return (
    <div className="form-editor">
      <div className="form-row form-row--inline">
        <div className="form-field">
          <label>Item Name</label>
          <input value={content.itemName} placeholder="Item name"
            onChange={(e) => update({ itemName: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Location</label>
          <input value={content.location} placeholder="Room / storage location"
            onChange={(e) => update({ location: e.target.value })} />
        </div>
      </div>

      <div className="form-field">
        <label>Description</label>
        <textarea value={content.description} rows={3} placeholder="Describe the item..."
          onChange={(e) => update({ description: e.target.value })} />
      </div>

      <div className="form-row form-row--inline">
        <div className="form-field">
          <label>Estimated Value ($)</label>
          <input type="number" value={content.value} min={0} step={0.01}
            onChange={(e) => update({ value: parseFloat(e.target.value) || 0 })} />
        </div>
        <div className="form-field">
          <label>Purchase Date</label>
          <input type="date" value={content.purchaseDate}
            onChange={(e) => update({ purchaseDate: e.target.value })} />
        </div>
      </div>

      <div className="form-row form-row--inline">
        <div className="form-field">
          <label>Serial Number</label>
          <input value={content.serialNumber} placeholder="S/N or model number"
            onChange={(e) => update({ serialNumber: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Warranty Expiry</label>
          <input type="date" value={content.warrantyExpiry}
            onChange={(e) => update({ warrantyExpiry: e.target.value })} />
        </div>
      </div>

      <div className="form-section">
        <div className="form-section-header">
          <h3>Photos <span className="photos-count">({images.length}/2)</span></h3>
          {images.length < 2 && (
            <button className="btn-add-item" onClick={() => fileInputRef.current?.click()}>+ Add Photo</button>
          )}
        </div>
        <input ref={fileInputRef} type="file" accept="image/*,application/pdf"
          style={{ display: 'none' }} onChange={handleImageUpload} />
        <div className="image-grid">
          {images.map((att) => (
            <div key={att.id} className="image-thumb">
              {att.mimeType.startsWith('image/') ? (
                <img src={att.url} alt={att.fileName} />
              ) : (
                <a href={att.url} target="_blank" rel="noopener noreferrer" className="pdf-thumb">
                  📄 {att.fileName}
                </a>
              )}
              <button
                className="image-thumb-delete"
                onClick={() => handleImageDelete(att)}
                title={`Delete ${att.fileName}`}
              >✕</button>
            </div>
          ))}
          {images.length === 0 && <p className="form-empty">No photos yet. Click + Add Photo.</p>}
        </div>
      </div>
    </div>
  );
};

export default InventoryEditor;
