package com.legacyvault.app.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyCacheTest {

    private lateinit var cache: KeyCache

    @BeforeEach
    fun setup() {
        cache = KeyCache()
    }

    @Test
    fun `set and get returns key`() {
        val key = ByteArray(32) { it.toByte() }
        cache.set("cat-1", key)
        val retrieved = cache.get("cat-1")
        assertNotNull(retrieved)
        assertTrue(retrieved!!.contentEquals(key))
    }

    @Test
    fun `has returns true after set`() {
        cache.set("cat-1", ByteArray(32))
        assertTrue(cache.has("cat-1"))
    }

    @Test
    fun `has returns false for unknown id`() {
        assertFalse(cache.has("unknown"))
    }

    @Test
    fun `get returns null for unknown id`() {
        assertNull(cache.get("unknown"))
    }

    @Test
    fun `clear removes single entry`() {
        cache.set("cat-1", ByteArray(32))
        cache.set("cat-2", ByteArray(32))
        cache.clear("cat-1")
        assertFalse(cache.has("cat-1"))
        assertTrue(cache.has("cat-2"))
    }

    @Test
    fun `clearAll removes all entries`() {
        cache.set("cat-1", ByteArray(32))
        cache.set("cat-2", ByteArray(32))
        cache.set("cat-3", ByteArray(32))
        cache.clearAll()
        assertFalse(cache.has("cat-1"))
        assertFalse(cache.has("cat-2"))
        assertFalse(cache.has("cat-3"))
        assertEquals(0, cache.size)
    }

    @Test
    fun `set overwrites existing key for same category`() {
        val key1 = ByteArray(32) { 1 }
        val key2 = ByteArray(32) { 2 }
        cache.set("cat-1", key1)
        cache.set("cat-1", key2)
        assertTrue(cache.get("cat-1")!!.contentEquals(key2))
    }

    @Test
    fun `size reflects number of cached entries`() {
        assertEquals(0, cache.size)
        cache.set("cat-1", ByteArray(32))
        assertEquals(1, cache.size)
        cache.set("cat-2", ByteArray(32))
        assertEquals(2, cache.size)
        cache.clear("cat-1")
        assertEquals(1, cache.size)
    }
}
