import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useDispatch } from 'react-redux';
import type { AppDispatch } from '../../store';
import { selectCategory } from '../../store/categoriesSlice';
import { setPendingSelect } from '../../store/pagesSlice';
import { searchApi, type SearchResult } from '../../api/searchApi';
import './GlobalSearch.css';

const PAGE_TYPE_ICONS: Record<string, string> = {
  Recipe: '🍽️', Quote: '💬', Note: '📝',
  HomeInventory: '🏠', Password: '🔑', Reminder: '🔔',
};

const GlobalSearch: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Debounced search
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (query.trim().length < 2) {
      setResults([]);
      setOpen(false);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      setLoading(true);
      try {
        const { data } = await searchApi.search(query.trim());
        setResults(data);
        setOpen(true);
        setFocusedIndex(-1);
      } catch {
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [query]);

  // Close on outside click
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  const navigateTo = useCallback((result: SearchResult) => {
    dispatch(setPendingSelect(result.pageId));
    dispatch(selectCategory(result.categoryId));
    setQuery('');
    setResults([]);
    setOpen(false);
  }, [dispatch]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!open || results.length === 0) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setFocusedIndex((i) => Math.min(i + 1, results.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setFocusedIndex((i) => Math.max(i - 1, 0));
    } else if (e.key === 'Enter' && focusedIndex >= 0) {
      e.preventDefault();
      navigateTo(results[focusedIndex]);
    } else if (e.key === 'Escape') {
      setOpen(false);
      inputRef.current?.blur();
    }
  };

  const highlight = (text: string, q: string) => {
    if (!q.trim()) return text;
    const idx = text.toLowerCase().indexOf(q.trim().toLowerCase());
    if (idx === -1) return text;
    return (
      <>
        {text.slice(0, idx)}
        <mark>{text.slice(idx, idx + q.trim().length)}</mark>
        {text.slice(idx + q.trim().length)}
      </>
    );
  };

  return (
    <div className="global-search" ref={containerRef}>
      <div className="global-search-input-wrap">
        <span className="global-search-icon">🔍</span>
        <input
          ref={inputRef}
          className="global-search-input"
          type="search"
          placeholder="Search all pages…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => results.length > 0 && setOpen(true)}
          autoComplete="off"
        />
        {loading && <span className="global-search-spinner" />}
      </div>

      {open && (
        <div className="global-search-dropdown">
          {results.length === 0 ? (
            <div className="global-search-empty">No results for "{query}"</div>
          ) : (
            <>
              <div className="global-search-count">{results.length} result{results.length !== 1 ? 's' : ''}</div>
              {results.map((r, i) => (
                <button
                  key={r.pageId}
                  className={`global-search-result${i === focusedIndex ? ' global-search-result--focused' : ''}`}
                  onMouseDown={(e) => { e.preventDefault(); navigateTo(r); }}
                  onMouseEnter={() => setFocusedIndex(i)}
                >
                  <span className="gsr-type">{PAGE_TYPE_ICONS[r.type] ?? '📄'}</span>
                  <div className="gsr-info">
                    <span className="gsr-title">{highlight(r.title, query)}</span>
                    <span className="gsr-category">
                      {r.categoryIcon} {r.categoryName}
                      {r.isEncrypted && <span className="gsr-lock"> 🔒</span>}
                    </span>
                  </div>
                  <span className="gsr-date">{new Date(r.updatedAt).toLocaleDateString()}</span>
                </button>
              ))}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default GlobalSearch;
