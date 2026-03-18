package com.legacyvault.app.ui.pages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.repository.CategoryRepository
import com.legacyvault.app.domain.repository.PageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PageListUiState(
    val isSyncing: Boolean    = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PageListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pageRepository: PageRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    private val _uiState = MutableStateFlow(PageListUiState())
    val uiState: StateFlow<PageListUiState> = _uiState.asStateFlow()

    val pages: StateFlow<List<PageSummary>> = pageRepository
        .observeByCategory(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val category: StateFlow<Category?> = categoryRepository
        .observeById(categoryId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            pageRepository.syncCategory(categoryId)
                .onFailure { e ->
                    _uiState.update { it.copy(isSyncing = false, errorMessage = e.message) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isSyncing = false) }
                }
        }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            pageRepository.delete(categoryId, pageId)
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun toggleFavorite(page: PageSummary) {
        viewModelScope.launch {
            pageRepository.setFavorite(categoryId, page.id, !page.isFavorite)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
