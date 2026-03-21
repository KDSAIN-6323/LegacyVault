package com.legacyvault.app.ui.auth

import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
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
 * Evicts all vault keys from [KeyCache] after the configured inactivity timeout.
 * Reset on every pointer/keyboard event via [onUserActivity].
 */
@Singleton
class InactivityManager @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val keyCache: KeyCache
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    fun start() { resetTimer() }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onUserActivity() { resetTimer() }

    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            val timeoutMinutes = prefs.inactivityTimeoutMinutes.first()
            delay(timeoutMinutes * 60_000L)
            keyCache.clearAll()
        }
    }
}
