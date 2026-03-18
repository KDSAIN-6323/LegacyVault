package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.CategoryType

data class Category(
    val id: String,
    val type: CategoryType,
    val name: String,
    val icon: String,
    val isEncrypted: Boolean,
    val encryptionSalt: String?,
    val passwordHint: String?,
    val isFavorite: Boolean,
    val pageCount: Int,
    val createdAt: String,
    val updatedAt: String
) {
    val isVault: Boolean get() = type == CategoryType.Vault
}
