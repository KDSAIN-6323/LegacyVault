package com.legacyvault.app.ui.navigation

/**
 * All navigation routes for the app.
 * Using a plain object with string constants keeps Navigation Compose
 * simple at this stage; typed routes (Kotlin Serializable) can be
 * adopted in Phase 7 when argument passing becomes complex.
 */
object Routes {

    // ── Auth graph ────────────────────────────────────────────────────────
    const val AUTH_GRAPH   = "auth"
    const val LOGIN        = "auth/login"
    const val REGISTER     = "auth/register"
    const val RESET_PW     = "auth/reset-password"
    const val API_URL      = "auth/api-url"

    // ── Main graph ────────────────────────────────────────────────────────
    const val MAIN_GRAPH   = "main"

    // Vault unlock (encrypted categories)
    const val VAULT_UNLOCK = "main/categories/{categoryId}/unlock"
    fun vaultUnlock(categoryId: String) = "main/categories/$categoryId/unlock"

    // Categories (Vaults)
    const val CATEGORY_LIST = "main/categories"
    const val CATEGORY_CREATE = "main/categories/create"

    // Pages
    const val PAGE_LIST    = "main/categories/{categoryId}/pages"
    const val PAGE_DETAIL  = "main/categories/{categoryId}/pages/{pageId}"
    const val PAGE_CREATE  = "main/categories/{categoryId}/pages/create"

    // Top-level destinations (bottom nav)
    const val SEARCH       = "main/search"
    const val REMINDERS    = "main/reminders"
    const val SHOPPING     = "main/shopping"
    const val SETTINGS     = "main/settings"

    // Settings sub-screens
    const val SETTINGS_API_URL = "main/settings/api-url"
    const val BACKUP           = "main/settings/backup"

    // ── Route builder helpers ─────────────────────────────────────────────
    fun pageList(categoryId: String)  = "main/categories/$categoryId/pages"
    fun pageDetail(categoryId: String, pageId: String) =
        "main/categories/$categoryId/pages/$pageId"
    fun pageCreate(categoryId: String) = "main/categories/$categoryId/pages/create"
}
