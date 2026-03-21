package com.legacyvault.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.ui.backup.BackupScreen
import com.legacyvault.app.ui.categories.CategoryListScreen
import com.legacyvault.app.ui.search.SearchScreen
import com.legacyvault.app.ui.pages.PageListScreen
import com.legacyvault.app.ui.pages.detail.PageDetailScreen
import com.legacyvault.app.ui.reminders.RemindersScreen
import com.legacyvault.app.ui.settings.SettingsScreen
import com.legacyvault.app.ui.shopping.ShoppingScreen
import com.legacyvault.app.ui.splash.SplashScreen
import com.legacyvault.app.ui.vault.VaultUnlockScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController    = navController,
        startDestination = Routes.SPLASH,
        modifier         = modifier
    ) {

        // ── Splash / entry ────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onEnter = {
                    navController.navigate(Routes.CATEGORY_LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
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
                },
                onOpenBackup   = { navController.navigate(Routes.BACKUP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // ── Page list ─────────────────────────────────────────────────────
        composable(
            route     = Routes.PAGE_LIST,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            PageListScreen(
                onPageClick  = { pageId ->
                    navController.navigate(Routes.pageDetail(categoryId, pageId))
                },
                onCreatePage = { pageType: PageType ->
                    navController.navigate(Routes.pageCreate(categoryId, pageType.name))
                },
                onBack       = { navController.popBackStack() }
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
            PageDetailScreen(onBack = { navController.popBackStack() })
        }

        // ── Page create ───────────────────────────────────────────────────
        composable(
            route     = Routes.PAGE_CREATE,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("pageType")   {
                    type         = NavType.StringType
                    defaultValue = ""
                    nullable     = true
                }
            )
        ) {
            PageDetailScreen(onBack = { navController.popBackStack() })
        }

        // ── Top-level destinations (bottom nav) ───────────────────────────
        composable(Routes.SEARCH) {
            SearchScreen(
                onPageClick = { categoryId, pageId ->
                    navController.navigate(Routes.pageDetail(categoryId, pageId))
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

        composable(Routes.REMINDERS) {
            RemindersScreen(
                onPageClick = { categoryId, pageId ->
                    navController.navigate(Routes.pageDetail(categoryId, pageId))
                },
                onNavigate  = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.CATEGORY_LIST) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }

        composable(Routes.SHOPPING) {
            ShoppingScreen(
                onPageClick = { categoryId, pageId ->
                    navController.navigate(Routes.pageDetail(categoryId, pageId))
                },
                onNavigate  = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.CATEGORY_LIST) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
}

