package com.legacyvault.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Root navigation graph.
 *
 * Phase 1 — all destinations render placeholder screens.
 * Real composables are wired in Phase 6 (auth) and Phase 7 (core app).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // ── Auth ─────────────────────────────────────────────────────────
        composable(Routes.API_URL) {
            PlaceholderScreen("API URL Setup")
        }
        composable(Routes.LOGIN) {
            PlaceholderScreen("Login")
        }
        composable(Routes.REGISTER) {
            PlaceholderScreen("Register")
        }
        composable(Routes.RESET_PW) {
            PlaceholderScreen("Reset Password")
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
        composable(Routes.SEARCH) {
            PlaceholderScreen("Search")
        }
        composable(Routes.REMINDERS) {
            PlaceholderScreen("Reminders")
        }
        composable(Routes.SHOPPING) {
            PlaceholderScreen("Shopping Lists")
        }
        composable(Routes.SETTINGS) {
            PlaceholderScreen("Settings")
        }
        composable(Routes.SETTINGS_API_URL) {
            PlaceholderScreen("API URL")
        }
        composable(Routes.BACKUP) {
            PlaceholderScreen("Backup & Restore")
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
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
