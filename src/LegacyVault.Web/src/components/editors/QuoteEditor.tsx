import React, { useState } from 'react';
import { QuoteContent } from '../../types';
import './FormEditor.css';

interface Props {
  content: QuoteContent;
  onChange: (content: QuoteContent) => void;
}

function buildCopyText(content: QuoteContent): string {
  let result = `"${content.text.trim()}"`;
  if (content.author.trim()) {
    result += ` — ${content.author.trim()}`;
    if (content.source.trim()) result += `, ${content.source.trim()}`;
  }
  return result;
}

const QuoteEditor: React.FC<Props> = ({ content, onChange }) => {
  const update = (patch: Partial<QuoteContent>) => onChange({ ...content, ...patch });
  const [copied, setCopied] = useState(false);
  const [tagInput, setTagInput] = useState('');

  const handleTagInput = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const tag = tagInput.trim().toLowerCase();
      if (tag && !content.tags.includes(tag)) {
        update({ tags: [...content.tags, tag] });
      }
      setTagInput('');
    }
  };

  const handleCopy = async () => {
    if (!content.text.trim()) return;
    await navigator.clipboard.writeText(buildCopyText(content));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (content.locked) {
    return (
      <div className="quote-card">
        <button
          className="recipe-lock-toggle"
          onClick={() => update({ locked: false })}
          title="Unlock to edit"
        >
          🔒 Locked
        </button>

        <div className="quote-card-mark">"</div>
        <blockquote className="quote-card-text">{content.text || <em className="quote-card-empty">No quote text</em>}</blockquote>

        {(content.author || content.source) && (
          <p className="quote-card-attribution">
            — {content.author}{content.author && content.source ? ', ' : ''}<cite>{content.source}</cite>
          </p>
        )}

        {content.tags.length > 0 && (
          <div className="quote-card-tags">
            {content.tags.map((tag) => (
              <span key={tag} className="tag">{tag}</span>
            ))}
          </div>
        )}

        <button
          type="button"
          className={`quote-copy-btn quote-copy-btn--card${copied ? ' quote-copy-btn--copied' : ''}`}
          onClick={handleCopy}
          disabled={!content.text.trim()}
          title="Copy quote with attribution"
        >
          {copied ? '✓ Copied' : '⎘ Copy'}
        </button>
      </div>
    );
  }

  return (
    <div className="form-editor">
      <div className="form-editor-header">
        <button
          className="editor-lock-btn"
          onClick={() => update({ locked: true })}
          title="Lock quote into card view"
        >
          🔓 Lock Quote
        </button>
      </div>

      <div className="form-field">
        <div className="quote-label-row">
          <label>Quote</label>
          <button
            type="button"
            className={`quote-copy-btn${copied ? ' quote-copy-btn--copied' : ''}`}
            onClick={handleCopy}
            disabled={!content.text.trim()}
            title="Copy quote with attribution"
          >
            {copied ? '✓ Copied' : '⎘ Copy'}
          </button>
        </div>
        <textarea
          value={content.text}
          rows={5}
          placeholder="Enter the quote text..."
          className="quote-text"
          onChange={(e) => update({ text: e.target.value })}
        />
      </div>

      <div className="form-row form-row--inline">
        <div className="form-field">
          <label>Author</label>
          <input value={content.author} placeholder="Author name"
            onChange={(e) => update({ author: e.target.value })} />
        </div>
        <div className="form-field">
          <label>Source</label>
          <input value={content.source} placeholder="Book, speech, movie..."
            onChange={(e) => update({ source: e.target.value })} />
        </div>
      </div>

      <div className="form-field">
        <label>Tags <small>(press Enter or comma to add)</small></label>
        <input
          className="tag-input-field"
          value={tagInput}
          placeholder="Add tag..."
          onChange={(e) => setTagInput(e.target.value)}
          onKeyDown={handleTagInput}
        />
        {content.tags.length > 0 && (
          <div className="tag-list">
            {content.tags.map((tag) => (
              <span key={tag} className="tag">
                {tag}
                <button onClick={() => update({ tags: content.tags.filter((t) => t !== tag) })}>×</button>
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default QuoteEditor;
