package com.legacyvault.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.domain.usecase.auth.LoginUseCase
import com.legacyvault.app.domain.usecase.auth.LogoutUseCase
import com.legacyvault.app.domain.usecase.auth.RegisterUseCase
import com.legacyvault.app.domain.usecase.auth.ResetPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean  = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Mirrors TokenStore.authState so the nav graph can react to login/logout. */
    val authState: StateFlow<TokenStore.AuthState> = tokenStore.authState
        .stateIn(
            scope            = viewModelScope,
            started          = SharingStarted.Eagerly,
            initialValue     = TokenStore.AuthState.Unauthenticated
        )

    // ── Login ──────────────────────────────────────────────────────────────

    fun login(username: String, password: String) {
        if (!validateNonEmpty(username, "Username") ||
            !validateNonEmpty(password, "Password")) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(username, password)
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.userMessage()) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    // Navigation is driven by authState changing to Authenticated
                }
        }
    }

    // ── Register ───────────────────────────────────────────────────────────

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (!validateNonEmpty(username, "Username") ||
            !validateNonEmpty(email, "Email") ||
            !validateNonEmpty(password, "Password")) return

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }
        if (password.length < 8) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            registerUseCase(username, email, password)
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.userMessage()) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    // ── Reset password ─────────────────────────────────────────────────────

    fun resetPassword(username: String, email: String, newPassword: String, confirmPassword: String) {
        if (!validateNonEmpty(username, "Username") ||
            !validateNonEmpty(email, "Email") ||
            !validateNonEmpty(newPassword, "New password")) return

        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            resetPasswordUseCase(username, email, newPassword)
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.userMessage()) }
                }
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "Password updated. Please log in.")
                    }
                }
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            // authState will emit Unauthenticated, nav graph redirects to login
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }

    private fun validateNonEmpty(value: String, fieldName: String): Boolean {
        if (value.isBlank()) {
            _uiState.update { it.copy(errorMessage = "$fieldName cannot be empty") }
            return false
        }
        return true
    }
}

private fun Throwable.userMessage(): String =
    when {
        message?.contains("401") == true  -> "Invalid credentials"
        message?.contains("409") == true  -> "Username or email already taken"
        message?.contains("404") == true  -> "Account not found"
        message?.contains("network") == true ||
        message?.contains("Unable to resolve") == true -> "Cannot reach server — check your connection"
        else -> message ?: "An unexpected error occurred"
    }
