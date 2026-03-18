package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.AttachmentDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.local.entity.toEntity
import com.legacyvault.app.data.remote.api.PagesApiService
import com.legacyvault.app.data.remote.dto.CreatePageRequest
import com.legacyvault.app.data.remote.dto.MovePageRequest
import com.legacyvault.app.data.remote.dto.UpdatePageRequest
import com.legacyvault.app.data.remote.mapper.toDomain
import com.legacyvault.app.data.remote.network.bodyOrThrow
import com.legacyvault.app.data.remote.network.throwIfError
import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.repository.PageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageRepositoryImpl @Inject constructor(
    private val pageDao: PageDao,
    private val attachmentDao: AttachmentDao,
    private val api: PagesApiService
) : PageRepository {

    // ── Observe ────────────────────────────────────────────────────────────

    override fun observeByCategory(categoryId: String): Flow<List<PageSummary>> =
        pageDao.observeByCategory(categoryId).map { it.map { e -> e.toSummary() } }

    override fun observeById(id: String): Flow<Page?> =
        pageDao.observeById(id).map { entity ->
            entity?.let {
                val attachments = attachmentDao.getByPage(id).map { a -> a.toDomain() }
                it.toDomain(attachments)
            }
        }

    override fun observeFavorites(): Flow<List<PageSummary>> =
        pageDao.observeFavorites().map { it.map { e -> e.toSummary() } }

    // ── Remote → cache ────────────────────────────────────────────────────

    override suspend fun syncCategory(categoryId: String): Result<List<PageSummary>> = runCatching {
        val summaries = api.getByCategory(categoryId).bodyOrThrow().map { it.toDomain() }
        pageDao.replaceByCategoryId(categoryId, summaries.map { it.toPageEntity(categoryId) })
        summaries
    }

    override suspend fun fetchPage(categoryId: String, pageId: String): Result<Page> = runCatching {
        val page = api.getById(categoryId, pageId).bodyOrThrow().toDomain()
        pageDao.upsert(page.toEntity())
        attachmentDao.replaceByPageId(pageId, page.attachments.map { it.toEntity(pageId) })
        page
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    override suspend fun create(
        categoryId: String,
        title: String,
        type: String,
        content: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        encryptionIV: String?
    ): Result<Page> = runCatching {
        val request = CreatePageRequest(title, type, content, isEncrypted, encryptionSalt, encryptionIV)
        val page    = api.create(categoryId, request).bodyOrThrow().toDomain()
        pageDao.upsert(page.toEntity())
        page
    }

    override suspend fun update(
        categoryId: String,
        pageId: String,
        title: String?,
        content: String?,
        encryptionIV: String?,
        sortOrder: Int?
    ): Result<Page> = runCatching {
        val request = UpdatePageRequest(title, content, encryptionIV, sortOrder)
        val page    = api.update(categoryId, pageId, request).bodyOrThrow().toDomain()
        pageDao.upsert(page.toEntity())
        page
    }

    override suspend fun delete(categoryId: String, pageId: String): Result<Unit> = runCatching {
        api.delete(categoryId, pageId).throwIfError()
        pageDao.deleteById(pageId)
    }

    override suspend fun move(
        categoryId: String,
        pageId: String,
        targetCategoryId: String
    ): Result<Page> = runCatching {
        val page = api.move(categoryId, pageId, MovePageRequest(targetCategoryId))
            .bodyOrThrow().toDomain()
        pageDao.moveTo(pageId, targetCategoryId)
        page
    }

    override suspend fun setFavorite(
        categoryId: String,
        pageId: String,
        isFavorite: Boolean
    ): Result<Unit> = runCatching {
        if (isFavorite) api.favorite(categoryId, pageId).throwIfError()
        else            api.unfavorite(categoryId, pageId).throwIfError()
        pageDao.setFavorite(pageId, isFavorite)
    }

    override suspend fun searchLocal(query: String): List<PageSummary> =
        pageDao.searchByTitle(query).map { it.toSummary() }
}

// ── Private helpers ────────────────────────────────────────────────────────

/** Build a minimal PageEntity from a PageSummary (no content — used for list caching). */
private fun PageSummary.toPageEntity(categoryId: String) =
    com.legacyvault.app.data.local.entity.PageEntity(
        id             = id,
        categoryId     = categoryId,
        type           = type.name,
        title          = title,
        content        = "",         // summaries have no content
        isEncrypted    = isEncrypted,
        encryptionSalt = null,
        encryptionIV   = null,
        isFavorite     = isFavorite,
        sortOrder      = sortOrder,
        createdAt      = updatedAt,  // no createdAt in summary
        updatedAt      = updatedAt
    )
