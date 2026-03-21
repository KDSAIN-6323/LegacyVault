package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.entity.toEntity
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.enums.CategoryType
import com.legacyvault.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observeById(id: String): Flow<Category?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeFavorites(): Flow<List<Category>> =
        dao.observeFavorites().map { it.map { e -> e.toDomain() } }

    override suspend fun create(
        name: String,
        icon: String,
        type: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        passwordHint: String?
    ): Result<Category> = runCatching {
        val now = Instant.now().toString()
        val category = Category(
            id             = UUID.randomUUID().toString(),
            type           = CategoryType.valueOf(type),
            name           = name,
            icon           = icon,
            isEncrypted    = isEncrypted,
            encryptionSalt = encryptionSalt,
            passwordHint   = passwordHint,
            isFavorite     = false,
            pageCount      = 0,
            createdAt      = now,
            updatedAt      = now
        )
        dao.upsert(category.toEntity())
        category
    }

    override suspend fun update(
        id: String,
        name: String,
        icon: String,
        passwordHint: String?
    ): Result<Category> = runCatching {
        val existing = dao.observeById(id).first()
            ?: throw NoSuchElementException("Category $id not found")
        val updated = existing.copy(
            name         = name,
            icon         = icon,
            passwordHint = passwordHint,
            updatedAt    = Instant.now().toString()
        )
        dao.upsert(updated)
        updated.toDomain()
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        dao.deleteById(id)
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean): Result<Unit> = runCatching {
        dao.setFavorite(id, isFavorite)
    }
}
