package com.legacyvault.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import com.legacyvault.app.data.remote.api.AuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiUrlUiState(
    val url: String       = "",
    val isValidating: Boolean = false,
    val errorMessage: String? = null,
    val isConfigured: Boolean = false   // true → navigate away
)

@HiltViewModel
class ApiUrlViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val api: AuthApiService          // uses auth client — no token needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiUrlUiState())
    val uiState: StateFlow<ApiUrlUiState> = _uiState.asStateFlow()

    val savedUrl: StateFlow<String> = prefs.apiBaseUrl
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesDataStore.DEFAULT_API_URL)

    fun onUrlChange(url: String) {
        _uiState.update { it.copy(url = url, errorMessage = null) }
    }

    /**
     * Validates the URL by hitting the /api/health endpoint, then persists it.
     * Validation is skipped in Settings (allowSkip = true) — the user may want
     * to pre-configure an offline server.
     */
    fun save(allowSkip: Boolean = false) {
        val raw = _uiState.value.url.trim()
        if (raw.isBlank()) {
            _uiState.update { it.copy(errorMessage = "URL cannot be empty") }
            return
        }
        if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            _uiState.update { it.copy(errorMessage = "URL must start with http:// or https://") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, errorMessage = null) }

            val reachable = if (allowSkip) true else runCatching {
                api.health().isSuccessful
            }.getOrDefault(false)

            if (!reachable) {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        errorMessage = "Could not reach server — check the URL and try again"
                    )
                }
                return@launch
            }

            prefs.setApiBaseUrl(raw)
            _uiState.update { it.copy(isValidating = false, isConfigured = true) }
        }
    }
}
