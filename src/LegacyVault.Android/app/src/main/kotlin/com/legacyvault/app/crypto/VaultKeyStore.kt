package com.legacyvault.app.crypto

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists biometric-encrypted vault key bytes in SharedPreferences.
 *
 * Each entry stores two keys:
 *   - "key_<categoryId>"  → Base64 of the Keystore-encrypted vault key bytes
 *   - "iv_<categoryId>"   → Base64 of the GCM IV needed to initialise the decrypt cipher
 *
 * Note: These bytes are useless without the Android Keystore key (which requires
 * biometric authentication), so plain SharedPreferences is acceptable here.
 * Using EncryptedSharedPreferences adds a defence-in-depth layer.
 */
@Singleton
class VaultKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "vault_bio_keys",
                androidx.security.crypto.MasterKey.Builder(context)
                    .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            // Fallback to plain SharedPreferences on devices that don't support
            // EncryptedSharedPreferences (e.g., old Robolectric test environments)
            context.getSharedPreferences("vault_bio_keys_fallback", Context.MODE_PRIVATE)
        }
    }

    /** Returns true if biometric-encrypted key bytes exist for [categoryId]. */
    fun has(categoryId: String): Boolean = prefs.contains(keyPref(categoryId))

    /**
     * Stores biometric-encrypted [encryptedKey] bytes and the associated [iv].
     * Both are Base64-encoded before storage.
     */
    fun store(categoryId: String, encryptedKey: ByteArray, iv: ByteArray) {
        prefs.edit()
            .putString(keyPref(categoryId), Base64.getEncoder().encodeToString(encryptedKey))
            .putString(ivPref(categoryId),  Base64.getEncoder().encodeToString(iv))
            .apply()
    }

    /**
     * Returns the stored (encryptedKey, iv) pair for [categoryId],
     * or null if not stored.
     */
    fun load(categoryId: String): Pair<ByteArray, ByteArray>? {
        val keyStr = prefs.getString(keyPref(categoryId), null) ?: return null
        val ivStr  = prefs.getString(ivPref(categoryId),  null) ?: return null
        return Base64.getDecoder().decode(keyStr) to Base64.getDecoder().decode(ivStr)
    }

    /** Removes stored biometric key data for [categoryId]. */
    fun remove(categoryId: String) {
        prefs.edit()
            .remove(keyPref(categoryId))
            .remove(ivPref(categoryId))
            .apply()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun keyPref(categoryId: String) = "key_$categoryId"
    private fun ivPref(categoryId: String)  = "iv_$categoryId"
}
