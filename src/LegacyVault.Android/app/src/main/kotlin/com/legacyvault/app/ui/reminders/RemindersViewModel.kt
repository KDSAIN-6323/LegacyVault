package com.legacyvault.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.model.enums.PageType
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

data class RemindersUiState(
    val errorMessage: String? = null
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val pageRepository: PageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    val reminders: StateFlow<List<PageSummary>> = pageRepository
        .observeByType(PageType.Reminder.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deletePage(categoryId: String, pageId: String) {
        viewModelScope.launch {
            pageRepository.delete(categoryId, pageId)
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun toggleFavorite(page: PageSummary) {
        viewModelScope.launch {
            pageRepository.setFavorite(page.categoryId, page.id, !page.isFavorite)
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
