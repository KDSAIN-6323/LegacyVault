package com.legacyvault.app.data.remote.network

import com.legacyvault.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory store for the short-lived JWT access token and current user.
 *
 * The access token is never written to disk — it lives only for the current
 * process. The refresh token is persisted separately via [PersistentCookieJar].
 *
 * Exposes [authState] as a [StateFlow] so the UI can react to login/logout
 * without polling.
 */
@Singleton
class TokenStore @Inject constructor() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val accessToken: String?
        get() = (_authState.value as? AuthState.Authenticated)?.accessToken

    val currentUser: User?
        get() = (_authState.value as? AuthState.Authenticated)?.user

    val isLoggedIn: Boolean
        get() = _authState.value is AuthState.Authenticated

    /** Called after a successful login or token refresh. */
    fun save(accessToken: String, user: User) {
        _authState.value = AuthState.Authenticated(accessToken, user)
    }

    /** Called on logout, inactivity timeout, or unrecoverable 401. */
    fun clear() {
        _authState.value = AuthState.Unauthenticated
    }

    /** Update only the access token after a silent refresh (user unchanged). */
    fun updateAccessToken(newToken: String) {
        val user = currentUser ?: return
        _authState.value = AuthState.Authenticated(newToken, user)
    }

    sealed class AuthState {
        data object Unauthenticated : AuthState()
        data class Authenticated(val accessToken: String, val user: User) : AuthState()
    }
}
