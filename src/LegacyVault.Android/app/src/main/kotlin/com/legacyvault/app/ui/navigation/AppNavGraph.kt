package com.legacyvault.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.legacyvault.app.data.remote.network.TokenStore
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.ui.auth.ApiUrlScreen
import com.legacyvault.app.ui.auth.AuthViewModel
import com.legacyvault.app.ui.auth.LoginScreen
import com.legacyvault.app.ui.auth.RegisterScreen
import com.legacyvault.app.ui.auth.ResetPasswordScreen
import com.legacyvault.app.ui.auth.StartupViewModel
import com.legacyvault.app.ui.categories.CategoryListScreen
import com.legacyvault.app.ui.pages.PageListScreen
import com.legacyvault.app.ui.pages.detail.PageDetailScreen
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
                onLoginSuccess            = {
                    navController.navigate(Routes.CATEGORY_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister      = { navController.navigate(Routes.REGISTER) },
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
        composable(
            route     = Routes.VAULT_UNLOCK,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            VaultUnlockScreen(
                onUnlocked = {
                    navController.navigate(Routes.pageList(categoryId)) {
                        popUpTo(Routes.VAULT_UNLOCK) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Categories (Vaults) ───────────────────────────────────────────
        composable(Routes.CATEGORY_LIST) {
            CategoryListScreen(
                onCategoryClick = { category, isUnlocked ->
                    if (category.isEncrypted && !isUnlocked) {
                        navController.navigate(Routes.vaultUnlock(category.id))
                    } else {
                        navController.navigate(Routes.pageList(category.id))
                    }
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.CATEGORY_LIST) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }

        // ── Page list ─────────────────────────────────────────────────────
        composable(
            route     = Routes.PAGE_LIST,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            PageListScreen(
                onPageClick   = { pageId ->
                    navController.navigate(Routes.pageDetail(categoryId, pageId))
                },
                onCreatePage  = { pageType: PageType ->
                    navController.navigate(Routes.pageCreate(categoryId, pageType.name))
                },
                onBack        = { navController.popBackStack() }
            )
        }

        // ── Page detail (edit) ────────────────────────────────────────────
        composable(
            route     = Routes.PAGE_DETAIL,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("pageId")     { type = NavType.StringType }
            )
        ) {
            PageDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Page create ───────────────────────────────────────────────────
        composable(
            route     = Routes.PAGE_CREATE,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("pageType")   {
                    type             = NavType.StringType
                    defaultValue     = ""
                    nullable         = true
                }
            )
        ) {
            PageDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Top-level destinations (bottom nav placeholders) ──────────────
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

    // Global logout observer — any screen triggers navigation back to Login
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
        color    = MaterialTheme.colorScheme.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.fillMaxSize()
        ) {
            androidx.compose.material3.Text(
                text  = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
