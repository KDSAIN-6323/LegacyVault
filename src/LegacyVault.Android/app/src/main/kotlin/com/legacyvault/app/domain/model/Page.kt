package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.PageType

data class PageSummary(
    val id: String,
    val categoryId: String,
    val type: PageType,
    val title: String,
    val isEncrypted: Boolean,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val updatedAt: String
)

/**
 * Full page including content. [content] is raw JSON for unencrypted pages,
 * or Base64 ciphertext for encrypted ones. Decryption happens in the VM layer.
 */
data class Page(
    val id: String,
    val categoryId: String,
    val type: PageType,
    val title: String,
    val content: String,
    val isEncrypted: Boolean,
    val encryptionIV: String?,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
) {
    fun toSummary() = PageSummary(
        id          = id,
        categoryId  = categoryId,
        type        = type,
        title       = title,
        isEncrypted = isEncrypted,
        isFavorite  = isFavorite,
        sortOrder   = sortOrder,
        updatedAt   = updatedAt
    )
}
