package com.legacyvault.app.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp [Interceptor] that attaches the Bearer access token to every outgoing
 * request (except those already carrying an Authorization header).
 *
 * The token is read from [TokenStore] on each call, so a silent refresh done
 * by [TokenRefreshAuthenticator] is automatically picked up without restarting
 * the request chain.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Don't overwrite an Authorization header that was set explicitly
        // (e.g. by the auth endpoints themselves).
        if (original.header("Authorization") != null) {
            return chain.proceed(original)
        }

        val token = tokenStore.accessToken
            ?: return chain.proceed(original)   // not logged in — send unauthenticated

        val authenticated = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticated)
    }
}
