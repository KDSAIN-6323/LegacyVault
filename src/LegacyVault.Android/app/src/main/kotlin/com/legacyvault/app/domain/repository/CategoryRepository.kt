package com.legacyvault.app.domain.repository

import com.legacyvault.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    fun observeById(id: String): Flow<Category?>
    fun observeFavorites(): Flow<List<Category>>

    suspend fun create(
        name: String,
        icon: String,
        type: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        passwordHint: String?
    ): Result<Category>

    suspend fun update(
        id: String,
        name: String,
        icon: String,
        passwordHint: String?
    ): Result<Category>

    suspend fun delete(id: String): Result<Unit>
    suspend fun setFavorite(id: String, isFavorite: Boolean): Result<Unit>
}
