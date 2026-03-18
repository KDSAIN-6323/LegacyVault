package com.legacyvault.app.ui.pages.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.crypto.CryptoService
import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.domain.repository.PageRepository
import com.legacyvault.app.domain.util.defaultContent
import com.legacyvault.app.domain.util.parsePageContent
import com.legacyvault.app.domain.util.serializePageContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PageDetailUiState(
    val title: String         = "",
    val content: PageContent? = null,   // null while loading
    val pageType: PageType?   = null,
    val isEncrypted: Boolean  = false,
    val isLocked: Boolean     = false,  // encrypted but key missing
    val isLoading: Boolean    = true,
    val isSaving: Boolean     = false,
    val isDirty: Boolean      = false,  // unsaved changes
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class PageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pageRepository: PageRepository,
    private val cryptoService: CryptoService,
    private val keyCache: KeyCache
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])
    private val pageId: String?    = savedStateHandle["pageId"]
    private val pageTypeArg: String? = savedStateHandle["pageType"]

    private val isCreateMode: Boolean = pageId == null || pageId == "new"

    private val _uiState = MutableStateFlow(PageDetailUiState())
    val uiState: StateFlow<PageDetailUiState> = _uiState.asStateFlow()

    init {
        if (isCreateMode) {
            val type = pageTypeArg?.let { runCatching { PageType.valueOf(it) }.getOrNull() }
                ?: PageType.Note
            _uiState.update {
                it.copy(
                    pageType  = type,
                    content   = defaultContent(type),
                    isLoading = false
                )
            }
        } else {
            loadPage()
        }
    }

    // ── Load ───────────────────────────────────────────────────────────────

    private fun loadPage() {
        viewModelScope.launch {
            pageRepository.observeById(pageId!!).collect { page ->
                if (page == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Page not found") }
                    return@collect
                }

                val rawContent = if (page.isEncrypted) {
                    val key = keyCache.get(categoryId)
                    if (key == null) {
                        _uiState.update {
                            it.copy(
                                title       = page.title,
                                pageType    = page.type,
                                isEncrypted = true,
                                isLocked    = true,
                                isLoading   = false
                            )
                        }
                        return@collect
                    }
                    runCatching {
                        cryptoService.decrypt(page.content, page.encryptionIV ?: "", key)
                    }.getOrElse { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Decrypt failed: ${e.message}") }
                        return@collect
                    }
                } else {
                    page.content
                }

                val parsed = parsePageContent(rawContent, page.type)
                _uiState.update {
                    it.copy(
                        title       = page.title,
                        content     = parsed,
                        pageType    = page.type,
                        isEncrypted = page.isEncrypted,
                        isLocked    = false,
                        isLoading   = false
                    )
                }
            }
        }
    }

    // ── Edit ───────────────────────────────────────────────────────────────

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, isDirty = true) }
    }

    fun onContentChange(content: PageContent) {
        _uiState.update { it.copy(content = content, isDirty = true) }
    }

    // ── Save ───────────────────────────────────────────────────────────────

    fun save() {
        val state = _uiState.value
        val content = state.content ?: return
        val title   = state.title.trim().ifBlank { state.pageType?.label ?: "Untitled" }
        val type    = state.pageType ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val jsonContent = serializePageContent(content)
            val (finalContent, finalIV) = if (state.isEncrypted) {
                val key = keyCache.get(categoryId)
                if (key == null) {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "Vault is locked") }
                    return@launch
                }
                val result = cryptoService.encrypt(jsonContent, key)
                result.ciphertext to result.iv
            } else {
                jsonContent to null
            }

            if (isCreateMode) {
                pageRepository.create(
                    categoryId     = categoryId,
                    title          = title,
                    type           = type.name,
                    content        = finalContent,
                    isEncrypted    = state.isEncrypted,
                    encryptionSalt = null,
                    encryptionIV   = finalIV
                ).onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isDirty = false, savedSuccessfully = true) }
                }
            } else {
                pageRepository.update(
                    categoryId   = categoryId,
                    pageId       = pageId!!,
                    title        = title,
                    content      = finalContent,
                    encryptionIV = finalIV,
                    sortOrder    = null
                ).onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isDirty = false) }
                }
            }
        }
    }

    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
    fun clearSavedFlag() = _uiState.update { it.copy(savedSuccessfully = false) }
}
