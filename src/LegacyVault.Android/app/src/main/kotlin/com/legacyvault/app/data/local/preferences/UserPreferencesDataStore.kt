package com.legacyvault.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/** Persistent user preferences backed by Jetpack DataStore. */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME                = stringPreferencesKey("theme")
        private val KEY_INACTIVITY_TIMEOUT   = intPreferencesKey("inactivity_timeout_minutes")
        private val KEY_FONT_SIZE            = stringPreferencesKey("font_size")

        const val DEFAULT_THEME              = "system"
        const val DEFAULT_INACTIVITY_MINUTES = 10
        const val DEFAULT_FONT_SIZE          = "medium"
    }

    // ── Theme ─────────────────────────────────────────────────────────────

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: DEFAULT_THEME
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[KEY_THEME] = theme }
    }

    // ── Inactivity timeout ────────────────────────────────────────────────

    val inactivityTimeoutMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_INACTIVITY_TIMEOUT] ?: DEFAULT_INACTIVITY_MINUTES
    }

    suspend fun setInactivityTimeout(minutes: Int) {
        context.dataStore.edit { it[KEY_INACTIVITY_TIMEOUT] = minutes }
    }

    // ── Font size ─────────────────────────────────────────────────────────

    val fontSize: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_FONT_SIZE] ?: DEFAULT_FONT_SIZE
    }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { it[KEY_FONT_SIZE] = size }
    }
}
