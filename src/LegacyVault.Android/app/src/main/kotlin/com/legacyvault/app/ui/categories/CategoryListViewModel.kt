package com.legacyvault.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.enums.CategoryType
import com.legacyvault.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryListUiState(
    val errorMessage: String?     = null,
    val showCreateSheet: Boolean  = false,
    val editingCategory: Category? = null
)

data class CategoryItem(
    val category: Category,
    val isUnlocked: Boolean     // true if key is in KeyCache
)

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val keyCache: KeyCache
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    /**
     * Combines the live Room category list with the current KeyCache state so
     * the UI can show lock/unlock indicators without an extra network call.
     */
    val categories: StateFlow<List<CategoryItem>> = repository.observeAll()
        .combine(
            // Re-emit whenever keyCache changes by polling a tick flow — a simple
            // approach since KeyCache is not a Flow source. The tick is driven by
            // uiState changes (lock/unlock events update uiState, triggering combine).
            _uiState
        ) { cats, _ ->
            cats.map { CategoryItem(it, !it.isEncrypted || keyCache.has(it.id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Create / Edit sheet ────────────────────────────────────────────────

    fun showCreateSheet()         = _uiState.update { it.copy(showCreateSheet = true, editingCategory = null) }
    fun showEditSheet(cat: Category) = _uiState.update { it.copy(showCreateSheet = true, editingCategory = cat) }
    fun dismissSheet()            = _uiState.update { it.copy(showCreateSheet = false, editingCategory = null) }

    fun saveCategory(
        name: String,
        icon: String,
        type: CategoryType,
        vaultPassword: String,
        passwordHint: String
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val existing = _uiState.value.editingCategory
        viewModelScope.launch {
            if (existing == null) {
                // Create
                val salt = if (type == CategoryType.Vault)
                    com.legacyvault.app.crypto.CryptoServiceImpl().generateSalt()
                else null

                repository.create(
                    name           = trimmedName,
                    icon           = icon.ifBlank { "📁" },
                    type           = type.name,
                    isEncrypted    = type == CategoryType.Vault,
                    encryptionSalt = salt,
                    passwordHint   = passwordHint.trim().ifBlank { null }
                ).onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            } else {
                // Update
                repository.update(
                    id           = existing.id,
                    name         = trimmedName,
                    icon         = icon.ifBlank { existing.icon },
                    passwordHint = passwordHint.trim().ifBlank { null }
                ).onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
            }
            dismissSheet()
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.delete(id).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun toggleFavorite(cat: Category) {
        viewModelScope.launch {
            repository.setFavorite(cat.id, !cat.isFavorite)
        }
    }

    fun lockVault(categoryId: String) {
        keyCache.clear(categoryId)
        // Touch uiState to trigger categories combine re-emit
        _uiState.update { it.copy() }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
