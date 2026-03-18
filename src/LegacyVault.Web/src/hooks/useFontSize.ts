import { useState, useEffect } from 'react';

export type FontSize = 'small' | 'medium' | 'large';

const SIZES: FontSize[] = ['small', 'medium', 'large'];
const STORAGE_KEY = 'lv_fontsize';

export function useFontSize(): [FontSize, (size: FontSize) => void] {
  const [size, setSize] = useState<FontSize>(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    const valid: FontSize = SIZES.includes(stored as FontSize) ? (stored as FontSize) : 'medium';
    document.documentElement.setAttribute('data-fontsize', valid); // apply immediately to avoid layout shift
    return valid;
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-fontsize', size);
    localStorage.setItem(STORAGE_KEY, size);
  }, [size]);

  return [size, setSize];
}
