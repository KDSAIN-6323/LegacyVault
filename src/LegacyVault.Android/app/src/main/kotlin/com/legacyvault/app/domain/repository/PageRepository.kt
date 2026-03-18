package com.legacyvault.app.domain.repository

import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import kotlinx.coroutines.flow.Flow

interface PageRepository {

    /** Live stream of page summaries for a category, ordered by sortOrder then updatedAt. */
    fun observeByCategory(categoryId: String): Flow<List<PageSummary>>

    /** Live stream of a single full page (with attachments). */
    fun observeById(id: String): Flow<Page?>

    /** Live stream of all favorite pages across all categories. */
    fun observeFavorites(): Flow<List<PageSummary>>

    /**
     * Fetch the page list for a category from the server, replace the local
     * cache for that category, and return the updated summaries.
     */
    suspend fun syncCategory(categoryId: String): Result<List<PageSummary>>

    /**
     * Fetch a single page (full content + attachments) from the server,
     * update the local cache, and return it.
     */
    suspend fun fetchPage(categoryId: String, pageId: String): Result<Page>

    suspend fun create(
        categoryId: String,
        title: String,
        type: String,
        content: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        encryptionIV: String?
    ): Result<Page>

    suspend fun update(
        categoryId: String,
        pageId: String,
        title: String?,
        content: String?,
        encryptionIV: String?,
        sortOrder: Int?
    ): Result<Page>

    suspend fun delete(categoryId: String, pageId: String): Result<Unit>

    suspend fun move(categoryId: String, pageId: String, targetCategoryId: String): Result<Page>

    suspend fun setFavorite(categoryId: String, pageId: String, isFavorite: Boolean): Result<Unit>

    /** Local-only search across cached page titles. */
    suspend fun searchLocal(query: String): List<PageSummary>
}
