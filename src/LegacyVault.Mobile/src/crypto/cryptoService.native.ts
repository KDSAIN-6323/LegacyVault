/**
 * Native crypto adapter for React Native using react-native-quick-crypto.
 * Provides the same interface as the web cryptoService.ts.
 *
 * react-native-quick-crypto uses JSI (native bridge) — much faster than
 * pure JS crypto. It mirrors the Node.js crypto API.
 */
import QuickCrypto from 'react-native-quick-crypto';

export const cryptoService = {
  generateSalt(): string {
    const bytes = QuickCrypto.randomBytes(32);
    return bytes.toString('base64');
  },

  async deriveKey(password: string, saltBase64: string): Promise<Buffer> {
    const salt = Buffer.from(saltBase64, 'base64');
    return new Promise((resolve, reject) => {
      QuickCrypto.pbkdf2(
        password,
        salt,
        310_000,
        32,           // 256-bit key
        'sha256',
        (err, key) => (err ? reject(err) : resolve(key!))
      );
    });
  },

  async encrypt(plaintext: string, key: Buffer): Promise<{ ciphertext: string; iv: string }> {
    const iv = QuickCrypto.randomBytes(12);  // 96-bit GCM IV
    const cipher = QuickCrypto.createCipheriv('aes-256-gcm', key, iv);
    const encrypted = Buffer.concat([
      cipher.update(plaintext, 'utf8'),
      cipher.final(),
    ]);
    const authTag = (cipher as any).getAuthTag();
    // Prepend auth tag to ciphertext (GCM auth tag is 16 bytes)
    const combined = Buffer.concat([authTag, encrypted]);
    return {
      ciphertext: combined.toString('base64'),
      iv: iv.toString('base64'),
    };
  },

  async decrypt(ciphertextBase64: string, ivBase64: string, key: Buffer): Promise<string> {
    const combined = Buffer.from(ciphertextBase64, 'base64');
    const iv = Buffer.from(ivBase64, 'base64');
    const authTag = combined.subarray(0, 16);
    const ciphertext = combined.subarray(16);
    const decipher = QuickCrypto.createDecipheriv('aes-256-gcm', key, iv);
    (decipher as any).setAuthTag(authTag);
    const decrypted = Buffer.concat([
      decipher.update(ciphertext),
      decipher.final(),
    ]);
    return decrypted.toString('utf8');
  },
};
