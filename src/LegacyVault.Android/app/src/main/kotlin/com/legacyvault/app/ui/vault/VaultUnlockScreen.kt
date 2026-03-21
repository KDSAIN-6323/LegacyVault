package com.legacyvault.app.ui.vault

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.crypto.Cipher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultUnlockScreen(
    onUnlocked: () -> Unit,
    onBack: () -> Unit,
    viewModel: VaultUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar  = remember { SnackbarHostState() }
    val context   = LocalContext.current

    // ── Navigate on unlock ────────────────────────────────────────────────────
    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) onUnlocked()
    }

    // ── Show errors as snackbar ───────────────────────────────────────────────
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // ── Show BiometricPrompt for enrollment ───────────────────────────────────
    LaunchedEffect(uiState.enrollCipher) {
        val cipher = uiState.enrollCipher ?: return@LaunchedEffect
        val activity = context as? FragmentActivity ?: run {
            viewModel.dismissEnrollCipher()
            return@LaunchedEffect
        }
        showBiometricPrompt(
            activity    = activity,
            title       = "Enable biometric unlock",
            subtitle    = "Authenticate to save your vault key for future biometric logins",
            cipher      = cipher,
            onSuccess   = { viewModel.onBiometricEnrollSuccess(it) },
            onError     = { viewModel.dismissEnrollCipher() }
        )
    }

    // ── Show BiometricPrompt for unlock ───────────────────────────────────────
    LaunchedEffect(uiState.unlockCipher) {
        val cipher = uiState.unlockCipher ?: return@LaunchedEffect
        val activity = context as? FragmentActivity ?: run {
            viewModel.dismissUnlockCipher()
            return@LaunchedEffect
        }
        showBiometricPrompt(
            activity    = activity,
            title       = "Biometric vault unlock",
            subtitle    = uiState.categoryName,
            cipher      = cipher,
            onSuccess   = { viewModel.onBiometricUnlockSuccess(it) },
            onError     = { viewModel.dismissUnlockCipher() }
        )
    }

    var password        by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Detect biometric availability for the enroll button
    val canEnrollBiometric = remember(context) {
        val bm = BiometricManager.from(context)
        bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(snackbarData = data) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = Icons.Default.Lock,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.padding(bottom = 16.dp)
            )

            Text("This vault is encrypted", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "Enter the vault password to access its contents.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.passwordHint?.let { hint ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Hint: $hint",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Vault password") },
                singleLine    = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.unlock(password) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick  = { viewModel.unlock(password) },
                enabled  = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Unlock vault")
                }
            }

            // ── Biometric button ───────────────────────────────────────────
            if (uiState.hasBiometric && canEnrollBiometric) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { viewModel.prepareBiometricUnlock() },
                    enabled  = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Use biometrics")
                }
            } else if (!uiState.hasBiometric && canEnrollBiometric && uiState.isUnlocked) {
                // Offer enrollment after a successful password unlock
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { viewModel.prepareBiometricEnroll() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Enable biometric unlock")
                }
            }
        }
    }
}

// ── BiometricPrompt helper ────────────────────────────────────────────────────

private fun showBiometricPrompt(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    cipher: Cipher,
    onSuccess: (Cipher) -> Unit,
    onError: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            result.cryptoObject?.cipher?.let(onSuccess) ?: onError()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onError()
        override fun onAuthenticationFailed() { /* keep prompt open */ }
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    BiometricPrompt(activity, executor, callback)
        .authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
}
