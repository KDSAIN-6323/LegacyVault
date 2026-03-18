package com.legacyvault.app.domain.model.enums

/**
 * Matches the server's PageType enum (serialized as strings via JsonStringEnumConverter).
 * Order mirrors the TypeScript union: 'Recipe' | 'Quote' | 'Note' | 'HomeInventory' |
 * 'Password' | 'Reminder' | 'ShoppingList'
 */
enum class PageType {
    Recipe,
    Quote,
    Note,
    HomeInventory,
    Password,
    Reminder,
    ShoppingList;

    /** Human-readable label for UI display. */
    val label: String get() = when (this) {
        Recipe        -> "Recipe"
        Quote         -> "Quote"
        Note          -> "Note"
        HomeInventory -> "Home Inventory"
        Password      -> "Password"
        Reminder      -> "Reminder"
        ShoppingList  -> "Shopping List"
    }

    /** Emoji icon used in page lists and pickers. */
    val icon: String get() = when (this) {
        Recipe        -> "🍽️"
        Quote         -> "💬"
        Note          -> "📝"
        HomeInventory -> "🏠"
        Password      -> "🔑"
        Reminder      -> "🔔"
        ShoppingList  -> "🛒"
    }
}
