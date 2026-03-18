package com.legacyvault.app.domain.usecase.auth

import com.legacyvault.app.data.local.LegacyVaultDatabase
import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.network.PersistentCookieJar
import com.legacyvault.app.data.remote.network.TokenStore
import javax.inject.Inject

/**
 * Logs the user out:
 * 1. Calls the server logout endpoint (best-effort — doesn't fail the local logout).
 * 2. Clears the in-memory access token.
 * 3. Removes the persisted refresh token cookie.
 * 4. Wipes the local Room cache so no data leaks to the next session.
 */
class LogoutUseCase @Inject constructor(
    private val api: AuthApiService,
    private val tokenStore: TokenStore,
    private val cookieJar: PersistentCookieJar,
    private val database: LegacyVaultDatabase
) {
    suspend operator fun invoke() {
        runCatching { api.logout() }   // fire-and-forget
        tokenStore.clear()
        cookieJar.clearRefreshToken()
        database.categoryDao().deleteAll()
        database.pageDao().deleteAll()
    }
}
