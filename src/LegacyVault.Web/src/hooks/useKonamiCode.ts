import { useEffect, useRef } from 'react';

// ↑ ↑ ↓ ↓ ← → ← → B A
const KONAMI_SEQUENCE = [
  'ArrowUp', 'ArrowUp',
  'ArrowDown', 'ArrowDown',
  'ArrowLeft', 'ArrowRight',
  'ArrowLeft', 'ArrowRight',
  'b', 'a',
];

/**
 * Fires `onActivate` once when the Konami code is typed anywhere on the page.
 * Uses a ref for the callback so callers don't need to memoize it.
 */
export function useKonamiCode(onActivate: () => void): void {
  const callbackRef = useRef(onActivate);
  callbackRef.current = onActivate;

  useEffect(() => {
    const buffer: string[] = [];

    const handler = (e: KeyboardEvent) => {
      buffer.push(e.key);
      if (buffer.length > KONAMI_SEQUENCE.length) {
        buffer.shift();
      }
      if (buffer.join(',') === KONAMI_SEQUENCE.join(',')) {
        buffer.length = 0;
        callbackRef.current();
      }
    };

    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);
}
