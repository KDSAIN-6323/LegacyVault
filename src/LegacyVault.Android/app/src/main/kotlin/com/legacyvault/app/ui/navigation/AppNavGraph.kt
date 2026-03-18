package com.legacyvault.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.legacyvault.app.ui.auth.StartupViewModel
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.ui.auth.ApiUrlScreen
import com.legacyvault.app.ui.auth.AuthViewModel
import com.legacyvault.app.ui.auth.LoginScreen
import com.legacyvault.app.ui.auth.RegisterScreen
import com.legacyvault.app.ui.auth.ResetPasswordScreen
import com.legacyvault.app.ui.auth.StartupViewModel
import com.legacyvault.app.ui.vault.VaultUnlockScreen

/**
 * Root navigation graph.
 *
 * Start destination is determined by [StartupViewModel]:
 *   - Not configured → API_URL (first-launch server setup)
 *   - Configured + not logged in → LOGIN
 *   - Configured + logged in → CATEGORY_LIST
 *
 * Auth screens share a single [AuthViewModel] scoped to the auth sub-graph
 * via `hiltViewModel(backStackEntry)` on the parent route.
 */
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startupVm: StartupViewModel = hiltViewModel()
    val startupState by startupVm.startDestination.collectAsStateWithLifecycle()

    if (startupState == null) {
        // Brief splash while reading DataStore
        Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    NavHost(
        navController    = navController,
        startDestination = startupState!!,
        modifier         = modifier
    ) {

        // ── First-launch server URL setup ─────────────────────────────────
        composable(Routes.API_URL) {
            ApiUrlScreen(
                isFirstLaunch = true,
                onConfigured  = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.API_URL) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Routes.CATEGORY_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToResetPassword = { navController.navigate(Routes.RESET_PW) }
            )
        }

        // ── Register ──────────────────────────────────────────────────────
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.CATEGORY_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── Reset password ────────────────────────────────────────────────
        composable(Routes.RESET_PW) {
            ResetPasswordScreen(
                onSuccess = { navController.popBackStack(Routes.LOGIN, inclusive = false) },
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Vault unlock ──────────────────────────────────────────────────
        composable(Routes.VAULT_UNLOCK) { backStackEntry ->
            VaultUnlockScreen(
                onUnlocked = {
                    val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                    navController.navigate(Routes.pageList(categoryId)) {
                        popUpTo(Routes.VAULT_UNLOCK) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Main — Categories ─────────────────────────────────────────────
        composable(Routes.CATEGORY_LIST) {
            PlaceholderScreen("Vaults")
        }
        composable(Routes.CATEGORY_CREATE) {
            PlaceholderScreen("Create Vault")
        }

        // ── Main — Pages ──────────────────────────────────────────────────
        composable(Routes.PAGE_LIST) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            PlaceholderScreen("Pages — $categoryId")
        }
        composable(Routes.PAGE_DETAIL) { backStackEntry ->
            val pageId = backStackEntry.arguments?.getString("pageId") ?: ""
            PlaceholderScreen("Page Detail — $pageId")
        }
        composable(Routes.PAGE_CREATE) {
            PlaceholderScreen("New Page")
        }

        // ── Main — Top-level ─────────────────────────────────────────────
        composable(Routes.SEARCH)    { PlaceholderScreen("Search") }
        composable(Routes.REMINDERS) { PlaceholderScreen("Reminders") }
        composable(Routes.SHOPPING)  { PlaceholderScreen("Shopping Lists") }
        composable(Routes.SETTINGS)  { PlaceholderScreen("Settings") }
        composable(Routes.SETTINGS_API_URL) {
            ApiUrlScreen(
                isFirstLaunch = false,
                onConfigured  = { navController.popBackStack() },
                onBack        = { navController.popBackStack() }
            )
        }
        composable(Routes.BACKUP) { PlaceholderScreen("Backup & Restore") }
    }

    // Global logout observer — any screen can trigger navigation back to Login
    // by watching TokenStore via AuthViewModel injected at the graph level.
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is TokenStore.AuthState.Unauthenticated) {
            val current = navController.currentDestination?.route
            if (current != Routes.LOGIN && current != Routes.API_URL) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text  = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
