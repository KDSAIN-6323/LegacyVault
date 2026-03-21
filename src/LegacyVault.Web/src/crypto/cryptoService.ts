/**
 * Client-side AES-256-GCM encryption using the Web Crypto API.
 * The server NEVER sees plaintext for encrypted content.
 *
 * Encryption parameters:
 * - Key derivation: PBKDF2, SHA-256, 310,000 iterations (OWASP 2023)
 * - Salt: 256-bit (32 bytes) random, stored per-category or per-page
 * - IV: 96-bit (12 bytes) random, stored per-write alongside ciphertext
 * - Cipher: AES-256-GCM (authenticated encryption — tamper-evident)
 */

/** Safe Base64 encoding — avoids spread-into-String.fromCharCode stack overflow on large buffers */
function bytesToBase64(bytes: Uint8Array): string {
  let binary = '';
  for (let i = 0; i < bytes.length; i++) binary += String.fromCharCode(bytes[i]);
  return btoa(binary);
}

export const cryptoService = {
  /** Generate a cryptographically random 256-bit salt */
  generateSalt(): string {
    return bytesToBase64(crypto.getRandomValues(new Uint8Array(32)));
  },

  /** Derive an AES-256-GCM CryptoKey from password + Base64 salt */
  async deriveKey(password: string, saltBase64: string): Promise<CryptoKey> {
    const salt = Uint8Array.from(atob(saltBase64), (c) => c.charCodeAt(0));
    const keyMaterial = await crypto.subtle.importKey(
      'raw',
      new TextEncoder().encode(password),
      'PBKDF2',
      false,
      ['deriveKey']
    );
    return crypto.subtle.deriveKey(
      { name: 'PBKDF2', salt, iterations: 310_000, hash: 'SHA-256' },
      keyMaterial,
      { name: 'AES-GCM', length: 256 },
      false,
      ['encrypt', 'decrypt']
    );
  },

  /** Encrypt a plaintext string. Returns { ciphertext, iv } both Base64-encoded. */
  async encrypt(plaintext: string, key: CryptoKey): Promise<{ ciphertext: string; iv: string }> {
    const iv = crypto.getRandomValues(new Uint8Array(12));  // 96-bit GCM IV
    const encoded = new TextEncoder().encode(plaintext);
    const cipherBuffer = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, encoded);
    return {
      ciphertext: bytesToBase64(new Uint8Array(cipherBuffer)),
      iv: bytesToBase64(iv),
    };
  },

  /** Decrypt a Base64 ciphertext using the stored IV and key.
   *
   * Supports two wire formats:
   *   Web format (native):    Base64([ciphertext][16-byte tag])  — tag appended
   *   Mobile format (Android/iOS): Base64([16-byte tag][ciphertext]) — tag prepended
   *
   * Tries web format first; on auth failure retries with tag moved to the end.
   */
  async decrypt(ciphertextBase64: string, ivBase64: string, key: CryptoKey): Promise<string> {
    const combined = Uint8Array.from(atob(ciphertextBase64), (c) => c.charCodeAt(0));
    const iv = Uint8Array.from(atob(ivBase64), (c) => c.charCodeAt(0));

    // Try web format first: [ciphertext || tag]
    try {
      const plainBuffer = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, combined);
      return new TextDecoder().decode(plainBuffer);
    } catch (_: unknown) {
      // Fall through — likely mobile format
    }

    // Retry with mobile format: [tag || ciphertext] → rearrange to [ciphertext || tag]
    if (combined.length < 16) throw new Error('Ciphertext too short');
    const tag = combined.subarray(0, 16);
    const ciphertext = combined.subarray(16);
    const reordered = new Uint8Array(ciphertext.length + 16);
    reordered.set(ciphertext, 0);
    reordered.set(tag, ciphertext.length);
    const plainBuffer = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, reordered);
    return new TextDecoder().decode(plainBuffer);
  },
};
