package com.legacyvault.app.ui.auth

import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks user inactivity and triggers a logout (or vault lock) when the
 * configured timeout is exceeded.
 *
 * ## Behaviour
 * - [onUserActivity] resets the countdown each time the user interacts with
 *   the app (called from the root composable via pointer-input tracking).
 * - When the timer fires, all vault keys are evicted from [KeyCache] first,
 *   then [LogoutUseCase] is invoked to clear the access token and wipe the
 *   local cache. The [TokenStore] state change triggers the nav graph to
 *   redirect back to Login.
 * - The manager is a singleton so the timer persists across recompositions.
 *
 * ## Integration
 * Call [start] once from `MainActivity.onCreate` or the root composable.
 * Call [onUserActivity] on every pointer/keyboard event.
 * Call [stop] if the app is backgrounded long enough to warrant a restart
 * (optional — the timer will fire naturally regardless).
 */
@Singleton
class InactivityManager @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val tokenStore: TokenStore,
    private val keyCache: KeyCache,
    private val logoutUseCase: LogoutUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    fun start() {
        resetTimer()
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    /** Call on every meaningful user interaction (tap, swipe, key press). */
    fun onUserActivity() {
        if (tokenStore.isLoggedIn) resetTimer()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            val timeoutMinutes = prefs.inactivityTimeoutMinutes.first()
            delay(timeoutMinutes * 60_000L)
            if (tokenStore.isLoggedIn) {
                // Evict all vault keys first so encrypted content is inaccessible
                keyCache.clearAll()
                logoutUseCase()
            }
        }
    }
}
