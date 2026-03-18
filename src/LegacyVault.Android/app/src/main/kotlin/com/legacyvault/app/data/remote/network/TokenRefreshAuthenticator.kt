package com.legacyvault.app.data.remote.network

import com.legacyvault.app.data.remote.api.AuthApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * OkHttp [Authenticator] that handles 401 Unauthorized responses by attempting
 * a silent token refresh before retrying the original request.
 *
 * ## Circular dependency avoidance
 * The main OkHttp client depends on this authenticator, which needs
 * [AuthApiService], which is built by a Retrofit instance that itself needs an
 * OkHttp client. To break the cycle we inject an [AuthApiService] built on top
 * of a *separate*, plain OkHttp client (`@Named("auth")`) that has no
 * interceptors or authenticator.
 *
 * [Provider] is used for lazy injection so Hilt resolves the graph correctly
 * even though this class is part of the `main` client's construction chain.
 *
 * ## Retry guard
 * If the response that triggered the authenticator already carried a Bearer
 * token (i.e. it was a retried request), and the refresh also fails, we return
 * `null` to stop retrying and let the caller handle the 401.
 */
@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val persistentCookieJar: PersistentCookieJar,
    @Named("auth") private val authApiServiceProvider: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Already retried once — give up to avoid an infinite loop.
        if (response.request.header("Authorization") != null &&
            response.priorResponse != null
        ) {
            return null
        }

        // No refresh token persisted — nothing we can do.
        if (!persistentCookieJar.hasRefreshToken) {
            tokenStore.clear()
            return null
        }

        val newToken = runBlocking {
            try {
                val refreshResponse = authApiServiceProvider.get().refresh()
                if (refreshResponse.isSuccessful) {
                    refreshResponse.body()?.accessToken
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }

        return if (newToken != null) {
            tokenStore.updateAccessToken(newToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            // Refresh failed — clear credentials and signal the UI.
            tokenStore.clear()
            persistentCookieJar.clearRefreshToken()
            null
        }
    }
}
