package com.legacyvault.app.ui.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.crypto.BiometricKeyStore
import com.legacyvault.app.crypto.CryptoService
import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.crypto.VaultKeyStore
import com.legacyvault.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

data class VaultUnlockUiState(
    val categoryName: String  = "",
    val passwordHint: String? = null,
    val hasBiometric: Boolean = false,   // biometric key was previously enrolled
    val isLoading: Boolean    = false,
    val errorMessage: String? = null,
    val isUnlocked: Boolean   = false,
    /** Non-null when the VM wants the screen to launch BiometricPrompt for enrollment. */
    val enrollCipher: Cipher? = null,
    /** Non-null when the VM wants the screen to launch BiometricPrompt for unlock. */
    val unlockCipher: Cipher? = null
)

/**
 * Handles vault password entry and optional biometric unlock for an encrypted category.
 *
 * Flow:
 *   Password unlock:
 *     1. [unlock] derives AES-256 key via PBKDF2 → [KeyCache.set]
 *     2. If user confirms enrollment → [prepareBiometricEnroll] → screen shows
 *        BiometricPrompt → [onBiometricEnrollSuccess] encrypts key → [VaultKeyStore.store]
 *
 *   Biometric unlock:
 *     1. [prepareBiometricUnlock] → screen shows BiometricPrompt with decrypt cipher
 *     2. [onBiometricUnlockSuccess] decrypts stored key bytes → [KeyCache.set]
 */
@HiltViewModel
class VaultUnlockViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
    private val cryptoService: CryptoService,
    private val keyCache: KeyCache,
    private val biometricKeyStore: BiometricKeyStore,
    private val vaultKeyStore: VaultKeyStore
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    // Temporarily holds the derived key between password unlock and biometric enrollment
    private var pendingKey: ByteArray? = null

    private val _uiState = MutableStateFlow(VaultUnlockUiState())
    val uiState: StateFlow<VaultUnlockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val category = categoryRepository.observeById(categoryId).first()
            _uiState.update {
                it.copy(
                    categoryName = category?.name ?: "",
                    passwordHint = category?.passwordHint,
                    hasBiometric = vaultKeyStore.has(categoryId)
                )
            }
        }
    }

    // ── Password unlock ───────────────────────────────────────────────────────

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
                key
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to derive key")
                }
            }.onSuccess { key ->
                pendingKey = key
                _uiState.update { it.copy(isLoading = false, isUnlocked = true) }
            }
        }
    }

    // ── Biometric enrollment (called after successful password unlock) ─────────

    /**
     * Prepares an encrypt-mode [Cipher] for biometric enrollment.
     * The screen should show BiometricPrompt with this cipher.
     * Call [onBiometricEnrollSuccess] when the prompt succeeds.
     */
    fun prepareBiometricEnroll() {
        runCatching { biometricKeyStore.encryptCipher(categoryId) }
            .onSuccess { cipher -> _uiState.update { it.copy(enrollCipher = cipher) } }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    /** Called when BiometricPrompt succeeds for enrollment. */
    fun onBiometricEnrollSuccess(authenticatedCipher: Cipher) {
        val key = pendingKey ?: return
        runCatching {
            val (encryptedKey, iv) = biometricKeyStore.encrypt(authenticatedCipher, key)
            vaultKeyStore.store(categoryId, encryptedKey, iv)
        }.onSuccess {
            _uiState.update { it.copy(enrollCipher = null, hasBiometric = true) }
        }.onFailure { e ->
            _uiState.update { it.copy(enrollCipher = null, errorMessage = e.message) }
        }
        pendingKey = null
    }

    fun dismissEnrollCipher() = _uiState.update { it.copy(enrollCipher = null) }

    // ── Biometric unlock ──────────────────────────────────────────────────────

    /**
     * Prepares a decrypt-mode [Cipher] for biometric unlock.
     * The screen should show BiometricPrompt with this cipher.
     * Call [onBiometricUnlockSuccess] when the prompt succeeds.
     */
    fun prepareBiometricUnlock() {
        val (_, iv) = vaultKeyStore.load(categoryId) ?: run {
            _uiState.update { it.copy(errorMessage = "Biometric data not found. Use your password.") }
            return
        }
        runCatching { biometricKeyStore.decryptCipher(categoryId, iv) }
            .onSuccess { cipher -> _uiState.update { it.copy(unlockCipher = cipher) } }
            .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
    }

    /** Called when BiometricPrompt succeeds for unlock. */
    fun onBiometricUnlockSuccess(authenticatedCipher: Cipher) {
        val (encryptedKey, _) = vaultKeyStore.load(categoryId) ?: run {
            _uiState.update { it.copy(unlockCipher = null, errorMessage = "Biometric data not found.") }
            return
        }
        runCatching {
            val key = biometricKeyStore.decrypt(authenticatedCipher, encryptedKey)
            keyCache.set(categoryId, key)
        }.onSuccess {
            _uiState.update { it.copy(unlockCipher = null, isUnlocked = true) }
        }.onFailure { e ->
            _uiState.update { it.copy(unlockCipher = null, errorMessage = e.message) }
        }
    }

    fun dismissUnlockCipher() = _uiState.update { it.copy(unlockCipher = null) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
