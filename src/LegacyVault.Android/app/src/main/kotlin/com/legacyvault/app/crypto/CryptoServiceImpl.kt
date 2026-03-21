package com.legacyvault.app.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class CryptoServiceImpl @Inject constructor() : CryptoService {

    companion object {
        private const val PBKDF2_ALGORITHM   = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS  = 310_000
        private const val KEY_LENGTH_BITS    = 256
        private const val SALT_LENGTH_BYTES  = 32
        private const val IV_LENGTH_BYTES    = 12
        private const val TAG_LENGTH_BITS    = 128
        private const val TAG_LENGTH_BYTES   = 16   // 128 / 8
        private const val AES_CIPHER         = "AES/GCM/NoPadding"
    }

    private val secureRandom = SecureRandom()

    // ── Salt ─────────────────────────────────────────────────────────────────

    override fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    // ── Key derivation ───────────────────────────────────────────────────────

    override suspend fun deriveKey(password: String, saltBase64: String): ByteArray =
        withContext(Dispatchers.Default) {
            val salt = Base64.getDecoder().decode(saltBase64)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                KEY_LENGTH_BITS
            )
            try {
                factory.generateSecret(spec).encoded
            } finally {
                spec.clearPassword()
            }
        }

    // ── Encrypt ──────────────────────────────────────────────────────────────

    /**
     * Produces mobile-native wire format: Base64([16-byte tag][ciphertext]).
     *
     * Java's AES/GCM/NoPadding Cipher.doFinal() returns [ciphertext || tag].
     * We split and rearrange to [tag || ciphertext] before Base64-encoding
     * so the output matches what react-native-quick-crypto produces.
     */
    override suspend fun encrypt(plaintext: String, key: ByteArray): CryptoResult =
        withContext(Dispatchers.Default) {
            val iv = ByteArray(IV_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
            val secretKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(AES_CIPHER)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))

            // Java output: [ciphertext || tag]
            val javaOutput = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Split: tag is the last TAG_LENGTH_BYTES bytes
            val ciphertextLen = javaOutput.size - TAG_LENGTH_BYTES
            val tag        = javaOutput.copyOfRange(ciphertextLen, javaOutput.size)
            val ciphertext = javaOutput.copyOf(ciphertextLen)

            // Mobile wire format: [tag || ciphertext]
            val combined = ByteArray(TAG_LENGTH_BYTES + ciphertextLen)
            System.arraycopy(tag,        0, combined, 0,                 TAG_LENGTH_BYTES)
            System.arraycopy(ciphertext, 0, combined, TAG_LENGTH_BYTES,  ciphertextLen)

            CryptoResult(
                ciphertext = Base64.getEncoder().encodeToString(combined),
                iv         = Base64.getEncoder().encodeToString(iv)
            )
        }

    // ── Decrypt ──────────────────────────────────────────────────────────────

    /**
     * Decrypts data in either wire format:
     * 1. Mobile format — [tag (bytes 0-15)][ciphertext (bytes 16+)]
     * 2. Web format    — [ciphertext][tag (last 16 bytes)]
     *
     * Tries mobile format first (matches data written by this app and the
     * Expo mobile client). Falls back to web format for data encrypted by
     * the React web client.
     *
     * @throws CryptoError.AuthenticationFailed if both formats fail tag verification.
     * @throws CryptoError.InvalidCiphertext if the combined bytes are too short.
     */
    override suspend fun decrypt(
        ciphertextBase64: String,
        ivBase64: String,
        key: ByteArray
    ): String = withContext(Dispatchers.Default) {
        val combined = Base64.getDecoder().decode(ciphertextBase64)
        val iv       = Base64.getDecoder().decode(ivBase64)

        if (combined.size < TAG_LENGTH_BYTES) {
            throw CryptoError.InvalidCiphertext(
                "Ciphertext too short: ${combined.size} bytes (minimum $TAG_LENGTH_BYTES)"
            )
        }

        val secretKey = SecretKeySpec(key, "AES")

        // Try mobile format: [tag || ciphertext] → rearrange to Java [ciphertext || tag]
        decryptAttempt(combined, iv, secretKey, mobileFormat = true)
            // Try web format: [ciphertext || tag] → already correct for Java
            ?: decryptAttempt(combined, iv, secretKey, mobileFormat = false)
            ?: throw CryptoError.AuthenticationFailed()
    }

    /**
     * Single decrypt attempt.
     * Returns plaintext on success, null on authentication failure (wrong format or key).
     */
    private fun decryptAttempt(
        combined: ByteArray,
        iv: ByteArray,
        secretKey: SecretKeySpec,
        mobileFormat: Boolean
    ): String? {
        return try {
            // Java's Cipher always expects [ciphertext || tag] for decryption
            val javaInput: ByteArray = if (mobileFormat) {
                // Mobile: [tag (0..15)][ciphertext (16..)] → swap to [ciphertext || tag]
                val tag        = combined.copyOf(TAG_LENGTH_BYTES)
                val ciphertext = combined.copyOfRange(TAG_LENGTH_BYTES, combined.size)
                ByteArray(ciphertext.size + TAG_LENGTH_BYTES).also { out ->
                    System.arraycopy(ciphertext, 0, out, 0,               ciphertext.size)
                    System.arraycopy(tag,        0, out, ciphertext.size, TAG_LENGTH_BYTES)
                }
            } else {
                // Web: [ciphertext || tag] — already in Java's expected layout
                combined
            }

            val cipher = Cipher.getInstance(AES_CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, iv))
            cipher.doFinal(javaInput).toString(Charsets.UTF_8)
        } catch (_: AEADBadTagException) {
            null   // authentication tag mismatch — wrong format or key
        } catch (_: BadPaddingException) {
            null   // bad padding — wrong format
        } catch (_: IllegalBlockSizeException) {
            null   // wrong block size
        } catch (_: InvalidAlgorithmParameterException) {
            null   // malformed IV
        } catch (_: InvalidKeyException) {
            null   // key rejected by JCE
        }
    }
}
