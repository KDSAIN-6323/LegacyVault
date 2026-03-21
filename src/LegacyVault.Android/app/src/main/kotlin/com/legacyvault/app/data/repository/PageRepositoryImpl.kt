package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.local.entity.toEntity
import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.model.enums.PageType
import com.legacyvault.app.domain.repository.PageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageRepositoryImpl @Inject constructor(
    private val pageDao: PageDao,
    private val categoryDao: CategoryDao
) : PageRepository {

    override fun observeByCategory(categoryId: String): Flow<List<PageSummary>> =
        pageDao.observeByCategory(categoryId).map { it.map { e -> e.toSummary() } }

    override fun observeById(id: String): Flow<Page?> =
        pageDao.observeById(id).map { it?.toDomain() }

    override fun observeFavorites(): Flow<List<PageSummary>> =
        pageDao.observeFavorites().map { it.map { e -> e.toSummary() } }

    override fun observeByType(type: String): Flow<List<PageSummary>> =
        pageDao.observeByType(type).map { it.map { e -> e.toSummary() } }

    override suspend fun create(
        categoryId: String,
        title: String,
        type: String,
        content: String,
        isEncrypted: Boolean,
        encryptionIV: String?
    ): Result<Page> = runCatching {
        val now = Instant.now().toString()
        val page = Page(
            id           = UUID.randomUUID().toString(),
            categoryId   = categoryId,
            type         = PageType.valueOf(type),
            title        = title,
            content      = content,
            isEncrypted  = isEncrypted,
            encryptionIV = encryptionIV,
            isFavorite   = false,
            sortOrder    = 0,
            createdAt    = now,
            updatedAt    = now
        )
        pageDao.upsert(page.toEntity())
        categoryDao.incrementPageCount(categoryId)
        page
    }

    override suspend fun update(
        categoryId: String,
        pageId: String,
        title: String,
        content: String,
        encryptionIV: String?
    ): Result<Page> = runCatching {
        val existing = pageDao.observeById(pageId).first()
            ?: throw NoSuchElementException("Page $pageId not found")
        val updated = existing.copy(
            title        = title,
            content      = content,
            encryptionIV = encryptionIV,
            updatedAt    = Instant.now().toString()
        )
        pageDao.upsert(updated)
        updated.toDomain()
    }

    override suspend fun delete(categoryId: String, pageId: String): Result<Unit> = runCatching {
        pageDao.deleteById(pageId)
        categoryDao.decrementPageCount(categoryId)
    }

    override suspend fun move(
        categoryId: String,
        pageId: String,
        targetCategoryId: String
    ): Result<Unit> = runCatching {
        pageDao.moveTo(pageId, targetCategoryId)
        categoryDao.decrementPageCount(categoryId)
        categoryDao.incrementPageCount(targetCategoryId)
    }

    override suspend fun setFavorite(
        categoryId: String,
        pageId: String,
        isFavorite: Boolean
    ): Result<Unit> = runCatching {
        pageDao.setFavorite(pageId, isFavorite)
    }

    override suspend fun searchLocal(query: String): List<PageSummary> =
        pageDao.searchByTitle(query).map { it.toSummary() }
}
