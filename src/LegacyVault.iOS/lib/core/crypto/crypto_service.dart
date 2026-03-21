import 'dart:typed_data';

abstract class CryptoService {
  /// Generate a new random 32-byte salt encoded as standard Base64.
  String generateSalt();

  /// Derive a 32-byte key from [password] and the Base64-encoded [saltBase64]
  /// using PBKDF2-SHA256 with 310,000 iterations.
  Uint8List deriveKey(String password, String saltBase64);

  /// Encrypt [plaintext] with AES-256-GCM using [key].
  ///
  /// Returns a record with:
  /// - [ciphertext]: Base64([16-byte auth tag][ciphertext bytes]) — tag prepended
  /// - [iv]: Base64(12-byte random IV)
  ({String ciphertext, String iv}) encrypt(String plaintext, Uint8List key);

  /// Decrypt [ciphertextBase64] (mobile wire format: tag prepended) with [ivBase64] and [key].
  ///
  /// Tries mobile format first (first 16 bytes = tag), then falls back to web
  /// format (last 16 bytes = tag).
  ///
  /// Returns the decrypted plaintext string, or throws on failure.
  String decrypt(String ciphertextBase64, String ivBase64, Uint8List key);
}
