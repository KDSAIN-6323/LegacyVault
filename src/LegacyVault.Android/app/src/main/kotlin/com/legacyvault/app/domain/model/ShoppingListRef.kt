package com.legacyvault.app.domain.model

/**
 * Lightweight reference returned by GET /api/shopping-lists.
 * Full content is loaded on demand via GET /api/pages/{id}.
 */
data class ShoppingListRef(
    val id: String,
    val categoryId: String,
    val title: String,
    val isEncrypted: Boolean
)
