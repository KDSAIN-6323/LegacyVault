/**
 * In-memory session key cache.
 * Keys are never persisted to localStorage/sessionStorage/IndexedDB.
 * Cache is automatically cleared when the page unloads.
 */

const cache = new Map<string, CryptoKey>();

export const keyCache = {
  set: (categoryId: string, key: CryptoKey) => cache.set(categoryId, key),
  get: (categoryId: string) => cache.get(categoryId),
  has: (categoryId: string) => cache.has(categoryId),
  clear: (categoryId: string) => cache.delete(categoryId),
  clearAll: () => cache.clear(),
};

// Clear all keys on page unload for security
window.addEventListener('beforeunload', () => cache.clear());
