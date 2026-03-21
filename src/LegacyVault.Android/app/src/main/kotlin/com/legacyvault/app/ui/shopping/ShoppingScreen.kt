package com.legacyvault.app.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.legacyvault.app.ui.main.MainBottomBar
import com.legacyvault.app.ui.navigation.Routes
import com.legacyvault.app.ui.reminders.TypedPageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    onPageClick: (categoryId: String, pageId: String) -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ShoppingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lists   by viewModel.lists.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar    = { TopAppBar(title = { Text("Shopping Lists") }) },
        bottomBar = { MainBottomBar(currentRoute = Routes.SHOPPING, onNavigate = onNavigate) },
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(snackbarData = data) } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (lists.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No shopping lists yet — create one from inside a vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(lists, key = { it.id }) { page ->
                        TypedPageCard(
                            page        = page,
                            onTap       = { onPageClick(page.categoryId, page.id) },
                            onToggleFav = { viewModel.toggleFavorite(page) },
                            onDelete    = { viewModel.deletePage(page.categoryId, page.id) }
                        )
                    }
                }
            }
        }
    }
}
