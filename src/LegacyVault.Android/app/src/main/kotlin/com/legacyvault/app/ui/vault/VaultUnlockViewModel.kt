package com.legacyvault.app.ui.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.crypto.CryptoService
import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUnlockUiState(
    val categoryName: String  = "",
    val passwordHint: String? = null,
    val isLoading: Boolean    = false,
    val errorMessage: String? = null,
    val isUnlocked: Boolean   = false
)

/**
 * Handles vault password entry for an encrypted category.
 *
 * On success it derives the AES-256-GCM key from the vault's salt and stores
 * it in [KeyCache] keyed by categoryId. All page decrypt/encrypt operations
 * in the vault read their key from [KeyCache].
 *
 * The key is automatically evicted when the inactivity timer fires (via
 * [InactivityManager]) or on explicit logout.
 */
@HiltViewModel
class VaultUnlockViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val cryptoService: CryptoService,
    private val keyCache: KeyCache
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    private val _uiState = MutableStateFlow(VaultUnlockUiState())
    val uiState: StateFlow<VaultUnlockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val category = categoryRepository.observeById(categoryId).first()
            _uiState.update {
                it.copy(
                    categoryName = category?.name ?: "",
                    passwordHint = category?.passwordHint
                )
            }
        }
    }

    fun unlock(password: String) {
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val salt = categoryRepository.observeById(categoryId).first()?.encryptionSalt
            if (salt == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Vault salt not found") }
                return@launch
            }

            runCatching {
                val key = cryptoService.deriveKey(password, salt)
                keyCache.set(categoryId, key)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to derive key")
                }
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isUnlocked = true) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
