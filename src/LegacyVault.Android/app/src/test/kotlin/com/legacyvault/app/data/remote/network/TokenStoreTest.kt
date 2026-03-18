package com.legacyvault.app.data.remote.network

import com.legacyvault.app.domain.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TokenStoreTest {

    private lateinit var store: TokenStore

    private val testUser = User(id = "user-1", username = "alice", email = "alice@example.com")

    @BeforeEach
    fun setUp() {
        store = TokenStore()
    }

    // ── Initial state ──────────────────────────────────────────────────────

    @Test
    fun `initial state is Unauthenticated`() = runTest {
        val state = store.authState.first()
        assertTrue(state is TokenStore.AuthState.Unauthenticated)
    }

    @Test
    fun `accessToken is null when unauthenticated`() {
        assertNull(store.accessToken)
    }

    @Test
    fun `currentUser is null when unauthenticated`() {
        assertNull(store.currentUser)
    }

    @Test
    fun `isLoggedIn is false when unauthenticated`() {
        assertFalse(store.isLoggedIn)
    }

    // ── save ───────────────────────────────────────────────────────────────

    @Test
    fun `save transitions to Authenticated`() = runTest {
        store.save("token-abc", testUser)
        val state = store.authState.first()
        assertTrue(state is TokenStore.AuthState.Authenticated)
    }

    @Test
    fun `save stores accessToken`() {
        store.save("token-abc", testUser)
        assertEquals("token-abc", store.accessToken)
    }

    @Test
    fun `save stores user`() {
        store.save("token-abc", testUser)
        assertEquals(testUser, store.currentUser)
    }

    @Test
    fun `isLoggedIn is true after save`() {
        store.save("token-abc", testUser)
        assertTrue(store.isLoggedIn)
    }

    // ── clear ──────────────────────────────────────────────────────────────

    @Test
    fun `clear transitions back to Unauthenticated`() = runTest {
        store.save("token-abc", testUser)
        store.clear()
        val state = store.authState.first()
        assertTrue(state is TokenStore.AuthState.Unauthenticated)
    }

    @Test
    fun `accessToken is null after clear`() {
        store.save("token-abc", testUser)
        store.clear()
        assertNull(store.accessToken)
    }

    @Test
    fun `isLoggedIn is false after clear`() {
        store.save("token-abc", testUser)
        store.clear()
        assertFalse(store.isLoggedIn)
    }

    // ── updateAccessToken ──────────────────────────────────────────────────

    @Test
    fun `updateAccessToken replaces token while preserving user`() {
        store.save("old-token", testUser)
        store.updateAccessToken("new-token")
        assertEquals("new-token", store.accessToken)
        assertEquals(testUser, store.currentUser)
    }

    @Test
    fun `updateAccessToken is a no-op when not authenticated`() {
        store.updateAccessToken("new-token")
        assertNull(store.accessToken)
        assertFalse(store.isLoggedIn)
    }

    // ── StateFlow emissions ────────────────────────────────────────────────

    @Test
    fun `authState emits new value on save`() = runTest {
        store.save("t1", testUser)
        val state = store.authState.first() as TokenStore.AuthState.Authenticated
        assertEquals("t1", state.accessToken)
        assertEquals(testUser, state.user)
    }

    @Test
    fun `authState emits updated token after updateAccessToken`() = runTest {
        store.save("t1", testUser)
        store.updateAccessToken("t2")
        val state = store.authState.first() as TokenStore.AuthState.Authenticated
        assertEquals("t2", state.accessToken)
    }
}
