package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.entity.toEntity
import com.legacyvault.app.data.remote.api.CategoriesApiService
import com.legacyvault.app.data.remote.dto.CreateCategoryRequest
import com.legacyvault.app.data.remote.dto.UpdateCategoryRequest
import com.legacyvault.app.data.remote.mapper.toDomain
import com.legacyvault.app.data.remote.network.bodyOrThrow
import com.legacyvault.app.data.remote.network.throwIfError
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val api: CategoriesApiService
) : CategoryRepository {

    // ── Observe (Room → domain) ────────────────────────────────────────────

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { it.map { e -> e.toDomain() } }

    override fun observeById(id: String): Flow<Category?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeFavorites(): Flow<List<Category>> =
        dao.observeFavorites().map { it.map { e -> e.toDomain() } }

    // ── Remote → cache ────────────────────────────────────────────────────

    override suspend fun sync(): Result<List<Category>> = runCatching {
        val response = api.getAll()
        val dtos     = response.bodyOrThrow()
        val domains  = dtos.map { it.toDomain() }
        dao.replaceAll(domains.map { it.toEntity() })
        domains
    }

    // ── Mutations ──────────────────────────────────────────────────────────

    override suspend fun create(
        name: String,
        icon: String,
        type: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        passwordHint: String?
    ): Result<Category> = runCatching {
        val request  = CreateCategoryRequest(name, icon, type, isEncrypted, encryptionSalt, passwordHint)
        val response = api.create(request)
        val domain   = response.bodyOrThrow().toDomain()
        dao.upsert(domain.toEntity())
        domain
    }

    override suspend fun update(
        id: String,
        name: String,
        icon: String,
        passwordHint: String?
    ): Result<Category> = runCatching {
        val request  = UpdateCategoryRequest(name, icon, passwordHint)
        val response = api.update(id, request)
        val domain   = response.bodyOrThrow().toDomain()
        dao.upsert(domain.toEntity())
        domain
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        api.delete(id).throwIfError()
        dao.deleteById(id)
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean): Result<Unit> = runCatching {
        if (isFavorite) api.favorite(id).throwIfError()
        else            api.unfavorite(id).throwIfError()
        dao.setFavorite(id, isFavorite)
    }
}
