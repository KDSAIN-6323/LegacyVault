package com.legacyvault.app.crypto

/**
 * Client-side AES-256-GCM encryption service.
 *
 * Contract (must match web cryptoService.ts and cryptoService.native.ts):
 * - Key derivation : PBKDF2-SHA256, 310 000 iterations, 32-byte output
 * - Salt           : 32 random bytes, standard Base64-encoded
 * - IV             : 12 random bytes, standard Base64-encoded
 * - Cipher         : AES-256-GCM, 128-bit authentication tag
 * - Wire format    : Base64([16-byte auth tag][ciphertext])  ← mobile-native layout
 *
 * NOTE on web ↔ mobile format difference:
 * The web client (Web Crypto API) stores [ciphertext][tag] (tag appended).
 * The mobile client (react-native-quick-crypto) stores [tag][ciphertext] (tag prepended).
 * The Android implementation follows the mobile format.
 * [decrypt] tries mobile format first, then falls back to web format for
 * backward compatibility with data encrypted by the web client.
 */
interface CryptoService {

    /**
     * Generate a cryptographically random 256-bit (32-byte) salt.
     * Returns standard Base64-encoded string.
     */
    fun generateSalt(): String

    /**
     * Derive a 256-bit AES key from [password] and a Base64-encoded [saltBase64].
     * Uses PBKDF2-HMAC-SHA256 with 310 000 iterations.
     * Returns raw key bytes — caller is responsible for zeroing after use.
     * Runs on [kotlinx.coroutines.Dispatchers.Default].
     */
    suspend fun deriveKey(password: String, saltBase64: String): ByteArray

    /**
     * Encrypt [plaintext] with [key] (raw bytes from [deriveKey]).
     * Returns a [CryptoResult] containing both values Base64-encoded.
     * A fresh random IV is generated for every call.
     * Runs on [kotlinx.coroutines.Dispatchers.Default].
     */
    suspend fun encrypt(plaintext: String, key: ByteArray): CryptoResult

    /**
     * Decrypt [ciphertextBase64] using [ivBase64] and [key].
     * Tries mobile wire format ([tag][ciphertext]) first, then falls back
     * to web wire format ([ciphertext][tag]) for cross-client compatibility.
     * Throws [CryptoError.AuthenticationFailed] if both formats fail.
     * Runs on [kotlinx.coroutines.Dispatchers.Default].
     */
    suspend fun decrypt(ciphertextBase64: String, ivBase64: String, key: ByteArray): String
}

/** Result of a successful [CryptoService.encrypt] call. Both values are standard Base64. */
data class CryptoResult(
    val ciphertext: String,     // Base64([16-byte tag][ciphertext bytes])
    val iv: String              // Base64(12-byte IV)
)
