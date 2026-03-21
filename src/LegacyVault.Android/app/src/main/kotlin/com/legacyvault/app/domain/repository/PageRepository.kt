package com.legacyvault.app.domain.repository

import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import kotlinx.coroutines.flow.Flow

interface PageRepository {
    fun observeByCategory(categoryId: String): Flow<List<PageSummary>>
    fun observeById(id: String): Flow<Page?>
    fun observeFavorites(): Flow<List<PageSummary>>
    fun observeByType(type: String): Flow<List<PageSummary>>

    suspend fun create(
        categoryId: String,
        title: String,
        type: String,
        content: String,
        isEncrypted: Boolean,
        encryptionIV: String?
    ): Result<Page>

    suspend fun update(
        categoryId: String,
        pageId: String,
        title: String,
        content: String,
        encryptionIV: String?
    ): Result<Page>

    suspend fun delete(categoryId: String, pageId: String): Result<Unit>
    suspend fun move(categoryId: String, pageId: String, targetCategoryId: String): Result<Unit>
    suspend fun setFavorite(categoryId: String, pageId: String, isFavorite: Boolean): Result<Unit>
    suspend fun searchLocal(query: String): List<PageSummary>
}
