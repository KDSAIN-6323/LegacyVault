package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val pageId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val type: String,           // PageType string value
    val title: String,
    val isEncrypted: Boolean,
    val updatedAt: String
)
