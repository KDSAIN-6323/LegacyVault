package com.legacyvault.app.crypto

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory session cache for derived AES-256 vault keys.
 *
 * Keys are held in memory only — never written to SharedPreferences,
 * DataStore, Room, or the Android Keystore. The cache is cleared on:
 *   - Explicit [clearAll] call (logout, inactivity timeout)
 *   - [clear] for a single category (lock vault)
 *   - Process death (OS memory reclamation)
 *
 * Key bytes are zeroed before removal to minimise the window where key
 * material lingers in the GC heap.
 *
 * Thread-safe: backed by [ConcurrentHashMap].
 */
@Singleton
class KeyCache @Inject constructor() {

    private val cache = ConcurrentHashMap<String, ByteArray>()

    /** Store [key] bytes for [categoryId]. Overwrites any existing entry. */
    fun set(categoryId: String, key: ByteArray) {
        // Zero the old value before replacing, if present
        cache.put(categoryId, key)?.zero()
    }

    /** Returns the cached key for [categoryId], or null if not unlocked. */
    fun get(categoryId: String): ByteArray? = cache[categoryId]

    /** Returns true if [categoryId] has a cached key (vault is unlocked). */
    fun has(categoryId: String): Boolean = cache.containsKey(categoryId)

    /**
     * Remove and zero the key for [categoryId].
     * No-op if the category is not in the cache.
     */
    fun clear(categoryId: String) {
        cache.remove(categoryId)?.zero()
    }

    /**
     * Remove and zero all cached keys.
     * Call on logout and on inactivity timeout.
     */
    fun clearAll() {
        val keys = cache.keys().toList()
        keys.forEach { id -> cache.remove(id)?.zero() }
    }

    /** Number of currently cached (unlocked) vaults. */
    val size: Int get() = cache.size

    // ── Private ───────────────────────────────────────────────────────────

    /** Zero-fill a byte array to prevent key material lingering in GC heap. */
    private fun ByteArray.zero() = fill(0)
}
