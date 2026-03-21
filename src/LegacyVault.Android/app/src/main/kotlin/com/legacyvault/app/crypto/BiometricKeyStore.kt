package com.legacyvault.app.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Android Keystore keys used to wrap vault keys for biometric unlock.
 *
 * Each encrypted category gets its own Keystore key named
 * "vault_bio_<categoryId>". The key requires biometric authentication
 * (or strong device credential) before any Cipher operation is permitted.
 *
 * Usage pattern:
 *   1. Password unlock → [encryptCipher] → BiometricPrompt CryptoObject → encrypt key bytes → store
 *   2. Biometric unlock → [decryptCipher] → BiometricPrompt CryptoObject → decrypt stored bytes
 */
@Singleton
class BiometricKeyStore @Inject constructor() {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val AES_CIPHER        = "AES/GCM/NoPadding"
        private const val KEY_SIZE_BITS     = 256
        private const val TAG_LENGTH_BITS   = 128

        private fun alias(categoryId: String) = "vault_bio_$categoryId"
    }

    /** Returns true if a biometric Keystore key exists for [categoryId]. */
    fun hasKey(categoryId: String): Boolean {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return ks.containsAlias(alias(categoryId))
    }

    /** Deletes the biometric Keystore key for [categoryId], if present. */
    fun deleteKey(categoryId: String) {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        ks.deleteEntry(alias(categoryId))
    }

    /**
     * Returns an encrypt-mode [Cipher] for [categoryId].
     *
     * Pass this as `CryptoObject(cipher)` to `BiometricPrompt`. After
     * biometric authentication succeeds the cipher can be used to encrypt
     * the vault key bytes via [encrypt].
     *
     * Creates the Keystore key if it doesn't exist yet.
     */
    fun encryptCipher(categoryId: String): Cipher {
        val key = getOrCreateKey(categoryId)
        return Cipher.getInstance(AES_CIPHER).also {
            it.init(Cipher.ENCRYPT_MODE, key)
        }
    }

    /**
     * Returns a decrypt-mode [Cipher] for [categoryId] initialised with [iv].
     *
     * Pass this as `CryptoObject(cipher)` to `BiometricPrompt`. After
     * authentication the cipher can be used to decrypt the stored key bytes.
     */
    fun decryptCipher(categoryId: String, iv: ByteArray): Cipher {
        val ks  = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val key = ks.getKey(alias(categoryId), null) as SecretKey
        return Cipher.getInstance(AES_CIPHER).also {
            it.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        }
    }

    /**
     * Encrypts [plainKey] bytes with an already-authenticated [cipher]
     * (obtained from [encryptCipher] after BiometricPrompt succeeds).
     *
     * @return Pair of (encryptedBytes, iv) — both must be stored alongside
     *         each other to reconstruct the decrypt cipher later.
     */
    fun encrypt(cipher: Cipher, plainKey: ByteArray): Pair<ByteArray, ByteArray> {
        val encrypted = cipher.doFinal(plainKey)
        return encrypted to cipher.iv
    }

    /**
     * Decrypts [encryptedKey] bytes with an already-authenticated [cipher]
     * (obtained from [decryptCipher] after BiometricPrompt succeeds).
     */
    fun decrypt(cipher: Cipher, encryptedKey: ByteArray): ByteArray =
        cipher.doFinal(encryptedKey)

    // ── Private ───────────────────────────────────────────────────────────────

    private fun getOrCreateKey(categoryId: String): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = ks.getKey(alias(categoryId), null) as? SecretKey
        if (existing != null) return existing

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        keyGen.init(
            KeyGenParameterSpec.Builder(
                alias(categoryId),
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setUserAuthenticationRequired(true)
                // Require fresh biometric auth for every use (no time-based validity)
                .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                .build()
        )
        return keyGen.generateKey()
    }
}
