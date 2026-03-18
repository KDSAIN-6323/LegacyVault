package com.legacyvault.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.data.local.preferences.UserPreferencesDataStore
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Determines the initial navigation destination during the app splash.
 *
 * Resolves to:
 *   - null          → still loading (show splash spinner)
 *   - API_URL       → first launch, server not yet configured
 *   - CATEGORY_LIST → server configured and session token present
 *   - LOGIN         → server configured but not authenticated
 */
@HiltViewModel
class StartupViewModel @Inject constructor(
    prefs: UserPreferencesDataStore,
    tokenStore: TokenStore
) : ViewModel() {

    val startDestination = combine(
        prefs.isApiUrlConfigured,
        tokenStore.authState
    ) { isConfigured, authState ->
        when {
            !isConfigured -> Routes.API_URL
            authState is TokenStore.AuthState.Authenticated -> Routes.CATEGORY_LIST
            else -> Routes.LOGIN
        }
    }.stateIn(
        scope          = viewModelScope,
        started        = SharingStarted.Eagerly,
        initialValue   = null    // null while DataStore hasn't emitted yet
    )
}
