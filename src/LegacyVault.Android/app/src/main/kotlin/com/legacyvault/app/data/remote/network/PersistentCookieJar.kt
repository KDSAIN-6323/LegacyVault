package com.legacyvault.app.data.remote.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp [CookieJar] that persists the server's httpOnly refresh-token cookie
 * across app restarts.
 *
 * The refresh token is stored in [EncryptedSharedPreferences] so it is encrypted
 * at rest using AES-256-GCM (key wrapped with AES-256-SIV in the Android Keystore).
 *
 * Only cookies named "refreshToken" are persisted; all other cookies are kept only
 * for the lifetime of the request (session cookies).
 */
@Singleton
class PersistentCookieJar @Inject constructor(
    @ApplicationContext private val context: Context
) : CookieJar {

    companion object {
        private const val PREFS_FILE_NAME     = "secure_cookie_store"
        private const val KEY_REFRESH_TOKEN   = "cookie_refreshToken"
        private const val REFRESH_COOKIE_NAME = "refreshToken"
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── CookieJar ──────────────────────────────────────────────────────────

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookie ->
            if (cookie.name == REFRESH_COOKIE_NAME) {
                prefs.edit()
                    .putString(KEY_REFRESH_TOKEN, cookie.value)
                    .apply()
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val value = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return emptyList()

        val cookie = Cookie.Builder()
            .name(REFRESH_COOKIE_NAME)
            .value(value)
            .domain(url.host)
            .path("/")
            .httpOnly()
            .secure()
            .build()

        return listOf(cookie)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Remove the persisted refresh token (called on logout). */
    fun clearRefreshToken() {
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
    }

    /** True if a refresh token is currently persisted. */
    val hasRefreshToken: Boolean
        get() = prefs.contains(KEY_REFRESH_TOKEN)
}
