package com.legacyvault.app.ui.navigation

/**
 * All navigation routes for the app.
 * Using a plain object with string constants keeps Navigation Compose
 * simple at this stage; typed routes (Kotlin Serializable) can be
 * adopted in Phase 7 when argument passing becomes complex.
 */
object Routes {

    // ── Splash / entry screen ─────────────────────────────────────────────
    const val SPLASH = "splash"

    // ── Vault unlock (encrypted categories) ──────────────────────────────
    const val VAULT_UNLOCK = "main/categories/{categoryId}/unlock"
    fun vaultUnlock(categoryId: String) = "main/categories/$categoryId/unlock"

    // ── Categories (Vaults) ───────────────────────────────────────────────
    const val CATEGORY_LIST = "main/categories"

    // ── Pages ─────────────────────────────────────────────────────────────
    const val PAGE_LIST   = "main/categories/{categoryId}/pages"
    const val PAGE_DETAIL = "main/categories/{categoryId}/pages/{pageId}"
    const val PAGE_CREATE = "main/categories/{categoryId}/pages/create?pageType={pageType}"

    // ── Top-level destinations (bottom nav) ───────────────────────────────
    const val SEARCH    = "main/search"
    const val REMINDERS = "main/reminders"
    const val SHOPPING  = "main/shopping"
    const val SETTINGS  = "main/settings"

    // ── Settings sub-screens ──────────────────────────────────────────────
    const val BACKUP = "main/settings/backup"

    // ── Route builder helpers ─────────────────────────────────────────────
    fun pageList(categoryId: String) = "main/categories/$categoryId/pages"
    fun pageDetail(categoryId: String, pageId: String) =
        "main/categories/$categoryId/pages/$pageId"
    fun pageCreate(categoryId: String, pageType: String = "") =
        "main/categories/$categoryId/pages/create?pageType=$pageType"
}
