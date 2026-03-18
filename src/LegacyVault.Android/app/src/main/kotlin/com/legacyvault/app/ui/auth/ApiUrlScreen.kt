package com.legacyvault.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Shown on first launch (before any URL is saved) and also from Settings.
 *
 * @param isFirstLaunch hides the back button and changes copy when true
 * @param onConfigured  called after the URL is validated and saved
 * @param onBack        called when the user taps the back arrow (Settings flow)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiUrlScreen(
    isFirstLaunch: Boolean = true,
    onConfigured: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: ApiUrlViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedUrl by viewModel.savedUrl.collectAsStateWithLifecycle()

    // Pre-fill with the persisted URL when opened from Settings
    var urlInput by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(savedUrl) {
        if (urlInput.isEmpty()) urlInput = savedUrl
    }

    // Navigate away once configured
    LaunchedEffect(uiState.isConfigured) {
        if (uiState.isConfigured) onConfigured()
    }

    Scaffold(
        topBar = {
            if (!isFirstLaunch) {
                TopAppBar(
                    title = { Text("Server URL") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFirstLaunch) {
                Text(
                    text = "Welcome to LegacyVault",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter the address of your LegacyVault server to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = {
                    urlInput = it
                    viewModel.onUrlChange(it)
                },
                label = { Text("Server URL") },
                placeholder = { Text("https://your-server.example.com") },
                isError = uiState.errorMessage != null,
                supportingText = uiState.errorMessage?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.save(allowSkip = !isFirstLaunch) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save(allowSkip = !isFirstLaunch) },
                enabled = !uiState.isValidating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isFirstLaunch) "Connect" else "Save")
                }
            }
        }
    }
}
