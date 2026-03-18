package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.PageType

/**
 * Lightweight summary used in page lists — matches the server's PageSummary projection.
 * Does not include content or attachments.
 */
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
 * Full page including content and attachments — returned by GET /api/pages/{id}.
 * [content] is raw JSON for unencrypted pages, or Base64 ciphertext for encrypted ones.
 * Decryption happens in the use-case layer; this model always stores the wire value.
 */
data class Page(
    val id: String,
    val categoryId: String,
    val type: PageType,
    val title: String,
    val content: String,
    val isEncrypted: Boolean,
    val encryptionSalt: String?,
    val encryptionIV: String?,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<Attachment>
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
