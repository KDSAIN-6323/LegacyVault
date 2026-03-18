/**
 * In-memory session key cache for mobile.
 * Keys are in-process only — cleared when the app is killed/backgrounded.
 * For production: consider clearing keys after app backgrounded for N minutes.
 */

const cache = new Map<string, Buffer>();

export const keyCache = {
  set: (categoryId: string, key: Buffer) => cache.set(categoryId, key),
  get: (categoryId: string) => cache.get(categoryId),
  has: (categoryId: string) => cache.has(categoryId),
  clear: (categoryId: string) => cache.delete(categoryId),
  clearAll: () => cache.clear(),
};
