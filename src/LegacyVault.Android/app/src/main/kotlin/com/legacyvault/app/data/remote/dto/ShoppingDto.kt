package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Matches ShoppingListRefDto record from ShoppingListsController.cs */
@Serializable
data class ShoppingListRefDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val isEncrypted: Boolean
)
