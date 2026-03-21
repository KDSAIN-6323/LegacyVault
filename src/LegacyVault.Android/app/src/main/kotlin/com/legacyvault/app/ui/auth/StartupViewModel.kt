package com.legacyvault.app.ui.auth

import androidx.lifecycle.ViewModel
import com.legacyvault.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor() : ViewModel() {
    // No auth gate — always go straight to the vault list.
    val startDestination: StateFlow<String?> = MutableStateFlow(Routes.CATEGORY_LIST)
}
