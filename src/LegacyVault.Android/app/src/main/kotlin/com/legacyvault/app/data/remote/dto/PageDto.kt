package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageSummaryDto(
    val id: String,
    val categoryId: String,
    val type: String,           // PageType string value — mapped in domain layer
    val title: String,
    val isEncrypted: Boolean,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val updatedAt: String
)

@Serializable
data class PageDto(
    val id: String,
    val categoryId: String,
    val type: String,
    val title: String,
    val content: String,        // Raw JSON (plain) or Base64 ciphertext (encrypted)
    val isEncrypted: Boolean,
    val encryptionSalt: String? = null,
    val encryptionIV: String? = null,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
    val attachments: List<AttachmentDto> = emptyList()
)

@Serializable
data class AttachmentDto(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val url: String
)

@Serializable
data class CreatePageRequest(
    val title: String,
    val type: String,           // PageType string value
    val content: String = "{}",
    val isEncrypted: Boolean = false,
    val encryptionSalt: String? = null,
    val encryptionIV: String? = null
)

@Serializable
data class UpdatePageRequest(
    val title: String? = null,
    val content: String? = null,
    val encryptionIV: String? = null,
    val sortOrder: Int? = null
)

@Serializable
data class MovePageRequest(
    val targetCategoryId: String
)
