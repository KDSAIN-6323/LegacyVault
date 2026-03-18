package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val type: String,           // "General" | "Vault" — mapped to CategoryType in domain
    val name: String,
    val icon: String,
    val isEncrypted: Boolean,
    val encryptionSalt: String? = null,
    val passwordHint: String? = null,
    val isFavorite: Boolean,
    val pageCount: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val icon: String = "📁",
    val type: String = "General",       // "General" | "Vault"
    val isEncrypted: Boolean = false,
    val encryptionSalt: String? = null,
    val passwordHint: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val icon: String,
    val passwordHint: String? = null
)
