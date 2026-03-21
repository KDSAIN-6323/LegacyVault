package com.legacyvault.app.ui.pages.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.crypto.CryptoService
import com.legacyvault.app.crypto.KeyCache
import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.model.ShoppingListItem
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.domain.repository.PageRepository
import com.legacyvault.app.domain.util.defaultContent
import com.legacyvault.app.domain.util.parsePageContent
import com.legacyvault.app.domain.util.serializePageContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
    val savedSuccessfully: Boolean = false,
    val showShoppingListPicker: Boolean = false,
    val addedToListName: String? = null
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

    /** All shopping list pages available as targets for "add ingredients". */
    val shoppingLists: StateFlow<List<PageSummary>> = pageRepository
        .observeByType(PageType.ShoppingList.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
                    categoryId  = categoryId,
                    title       = title,
                    type        = type.name,
                    content     = finalContent,
                    isEncrypted = state.isEncrypted,
                    encryptionIV = finalIV
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
                    encryptionIV = finalIV
                ).onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isDirty = false) }
                }
            }
        }
    }

    // ── Shopping list integration ───────────────────────────────────────────

    fun showShoppingListPicker() = _uiState.update { it.copy(showShoppingListPicker = true) }
    fun dismissShoppingListPicker() = _uiState.update { it.copy(showShoppingListPicker = false) }
    fun clearAddedToListName() = _uiState.update { it.copy(addedToListName = null) }

    /**
     * Appends each recipe ingredient as a new unchecked item on [target].
     * Handles both plain and encrypted shopping list pages (if the vault key
     * is already cached); shows an error snackbar if the vault is locked.
     */
    fun addIngredientsToShoppingList(target: PageSummary, ingredients: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(showShoppingListPicker = false) }
            runCatching {
                val page = pageRepository.observeById(target.id).first()
                    ?: error("Shopping list not found")

                val rawContent = if (page.isEncrypted) {
                    val key = keyCache.get(page.categoryId)
                        ?: error("\"${target.title}\" vault is locked — unlock it first")
                    cryptoService.decrypt(page.content, page.encryptionIV ?: "", key)
                } else {
                    page.content
                }

                val existing = parsePageContent(rawContent, PageType.ShoppingList)
                    as PageContent.ShoppingList
                val newItems = ingredients.map { ingredient ->
                    ShoppingListItem(
                        id       = UUID.randomUUID().toString(),
                        name     = ingredient,
                        quantity = ""
                    )
                }
                val merged  = existing.copy(items = existing.items + newItems)
                val newJson = serializePageContent(merged)

                val (finalContent, finalIV) = if (page.isEncrypted) {
                    val key = keyCache.get(page.categoryId)!!
                    val result = cryptoService.encrypt(newJson, key)
                    result.ciphertext to result.iv
                } else {
                    newJson to null
                }

                pageRepository.update(
                    categoryId   = page.categoryId,
                    pageId       = page.id,
                    title        = page.title,
                    content      = finalContent,
                    encryptionIV = finalIV
                ).getOrThrow()

                page.title
            }.onSuccess { listName ->
                _uiState.update { it.copy(addedToListName = listName) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Could not add to list: ${e.message}") }
            }
        }
    }

    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
    fun clearSavedFlag() = _uiState.update { it.copy(savedSuccessfully = false) }
}
