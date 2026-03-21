package com.legacyvault.app.data.backup

import com.legacyvault.app.data.local.entity.CategoryEntity
import com.legacyvault.app.data.local.entity.PageEntity
import kotlinx.serialization.Serializable

@Serializable
data class VaultBackup(
    val version: Int = 1,
    val exportedAt: String,
    val categories: List<CategoryBackupDto>,
    val pages: List<PageBackupDto>
)

@Serializable
data class CategoryBackupDto(
    val id: String,
    val name: String,
    val icon: String,
    val type: String,
    val isEncrypted: Boolean,
    val encryptionSalt: String? = null,
    val passwordHint: String? = null,
    val isFavorite: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PageBackupDto(
    val id: String,
    val categoryId: String,
    val type: String,
    val title: String,
    val content: String,       // raw JSON for plaintext; Base64 ciphertext for encrypted pages
    val isEncrypted: Boolean,
    val encryptionIV: String? = null,
    val isFavorite: Boolean,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String
)

// ── Entity ↔ DTO mappings ─────────────────────────────────────────────────────

fun CategoryEntity.toBackupDto() = CategoryBackupDto(
    id             = id,
    name           = name,
    icon           = icon,
    type           = type,
    isEncrypted    = isEncrypted,
    encryptionSalt = encryptionSalt,
    passwordHint   = passwordHint,
    isFavorite     = isFavorite,
    createdAt      = createdAt,
    updatedAt      = updatedAt
)

fun CategoryBackupDto.toEntity() = CategoryEntity(
    id             = id,
    name           = name,
    icon           = icon,
    type           = type,
    isEncrypted    = isEncrypted,
    encryptionSalt = encryptionSalt,
    passwordHint   = passwordHint,
    isFavorite     = isFavorite,
    pageCount      = 0,   // recalculated after import
    createdAt      = createdAt,
    updatedAt      = updatedAt
)

fun PageEntity.toBackupDto() = PageBackupDto(
    id           = id,
    categoryId   = categoryId,
    type         = type,
    title        = title,
    content      = content,
    isEncrypted  = isEncrypted,
    encryptionIV = encryptionIV,
    isFavorite   = isFavorite,
    sortOrder    = sortOrder,
    createdAt    = createdAt,
    updatedAt    = updatedAt
)

fun PageBackupDto.toEntity() = PageEntity(
    id           = id,
    categoryId   = categoryId,
    type         = type,
    title        = title,
    content      = content,
    isEncrypted  = isEncrypted,
    encryptionIV = encryptionIV,
    isFavorite   = isFavorite,
    sortOrder    = sortOrder,
    createdAt    = createdAt,
    updatedAt    = updatedAt
)
