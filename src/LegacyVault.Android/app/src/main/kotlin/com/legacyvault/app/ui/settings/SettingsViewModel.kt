package com.legacyvault.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: String = UserPreferencesDataStore.DEFAULT_THEME,
    val inactivityTimeoutMinutes: Int = UserPreferencesDataStore.DEFAULT_INACTIVITY_MINUTES,
    val fontSize: String = UserPreferencesDataStore.DEFAULT_FONT_SIZE
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.theme,
        prefs.inactivityTimeoutMinutes,
        prefs.fontSize
    ) { theme, timeout, fontSize ->
        SettingsUiState(
            theme = theme,
            inactivityTimeoutMinutes = timeout,
            fontSize = fontSize
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: String) {
        viewModelScope.launch { prefs.setTheme(theme) }
    }

    fun setInactivityTimeout(minutes: Int) {
        viewModelScope.launch { prefs.setInactivityTimeout(minutes) }
    }

    fun setFontSize(size: String) {
        viewModelScope.launch { prefs.setFontSize(size) }
    }
}
