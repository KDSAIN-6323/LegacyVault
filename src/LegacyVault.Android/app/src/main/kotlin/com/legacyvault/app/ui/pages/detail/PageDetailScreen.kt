package com.legacyvault.app.ui.pages.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.ui.pages.editors.HomeInventoryEditor
import com.legacyvault.app.ui.pages.editors.NoteEditor
import com.legacyvault.app.ui.pages.editors.PasswordEditor
import com.legacyvault.app.ui.pages.editors.QuoteEditor
import com.legacyvault.app.ui.pages.editors.RecipeEditor
import com.legacyvault.app.ui.pages.editors.ReminderEditor
import com.legacyvault.app.ui.pages.editors.ShoppingListEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailScreen(
    onBack: () -> Unit,
    onSavedAndNew: (() -> Unit)? = null,
    viewModel: PageDetailViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val shoppingLists by viewModel.shoppingLists.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Navigate back after create-mode save
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            viewModel.clearSavedFlag()
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.addedToListName) {
        uiState.addedToListName?.let {
            snackbar.showSnackbar("Ingredients added to \"$it\"")
            viewModel.clearAddedToListName()
        }
    }

    // Intercept back press to show discard confirmation
    BackHandler(enabled = uiState.isDirty) {
        showDiscardDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.pageType?.let { Text(it.label) }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isDirty) showDiscardDialog = true else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isEncrypted) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(
                        onClick  = viewModel::save,
                        enabled  = !uiState.isSaving && !uiState.isLocked
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(snackbarData = data) } }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.isLocked -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Vault is locked. Go back and unlock it first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.content != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    // Title field
                    OutlinedTextField(
                        value         = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        placeholder   = { Text("Title") },
                        textStyle     = MaterialTheme.typography.headlineSmall,
                        singleLine    = true,
                        colors        = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor   = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    // Type-specific editor
                    PageContentEditor(
                        content              = uiState.content!!,
                        pageType             = uiState.pageType!!,
                        onContentChange      = viewModel::onContentChange,
                        onAddToShoppingList  = if (uiState.pageType == PageType.Recipe)
                            viewModel::showShoppingListPicker else null,
                        modifier             = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title   = { Text("Discard changes?") },
            text    = { Text("Your unsaved changes will be lost.") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
        )
    }

    if (uiState.showShoppingListPicker) {
        val recipe = uiState.content as? PageContent.Recipe
        AlertDialog(
            onDismissRequest = viewModel::dismissShoppingListPicker,
            title = { Text("Add to shopping list") },
            text  = {
                if (shoppingLists.isEmpty()) {
                    Text("No shopping lists found. Create one inside a vault first.")
                } else {
                    LazyColumn {
                        items(shoppingLists) { list ->
                            TextButton(
                                onClick = {
                                    viewModel.addIngredientsToShoppingList(
                                        target      = list,
                                        ingredients = recipe?.ingredients ?: emptyList()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(list.title, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::dismissShoppingListPicker) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PageContentEditor(
    content: PageContent,
    pageType: PageType,
    onContentChange: (PageContent) -> Unit,
    onAddToShoppingList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (pageType) {
        PageType.Note -> NoteEditor(
            content         = content as? PageContent.Note ?: PageContent.Note(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
        PageType.Password -> PasswordEditor(
            content         = content as? PageContent.Password ?: PageContent.Password(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
        PageType.Recipe -> RecipeEditor(
            content              = content as? PageContent.Recipe ?: PageContent.Recipe(),
            onContentChange      = { onContentChange(it) },
            onAddToShoppingList  = onAddToShoppingList,
            modifier             = modifier
        )
        PageType.Quote -> QuoteEditor(
            content         = content as? PageContent.Quote ?: PageContent.Quote(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
        PageType.HomeInventory -> HomeInventoryEditor(
            content         = content as? PageContent.HomeInventory ?: PageContent.HomeInventory(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
        PageType.Reminder -> ReminderEditor(
            content         = content as? PageContent.Reminder ?: PageContent.Reminder(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
        PageType.ShoppingList -> ShoppingListEditor(
            content         = content as? PageContent.ShoppingList ?: PageContent.ShoppingList(),
            onContentChange = { onContentChange(it) },
            modifier        = modifier
        )
    }
}
