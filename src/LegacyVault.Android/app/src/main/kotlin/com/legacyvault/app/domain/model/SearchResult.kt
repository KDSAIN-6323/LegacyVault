package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.PageType

data class SearchResult(
    val pageId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val type: PageType,
    val title: String,
    val isEncrypted: Boolean,
    val updatedAt: String
)
