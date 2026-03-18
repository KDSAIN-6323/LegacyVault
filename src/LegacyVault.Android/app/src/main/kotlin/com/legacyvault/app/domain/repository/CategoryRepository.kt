package com.legacyvault.app.domain.repository

import com.legacyvault.app.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    /** Live stream of all cached categories, ordered favorites-first then by name. */
    fun observeAll(): Flow<List<Category>>

    /** Live stream of a single category; emits null if deleted. */
    fun observeById(id: String): Flow<Category?>

    /** Live stream of favorite categories. */
    fun observeFavorites(): Flow<List<Category>>

    /**
     * Fetch the full category list from the server, replace the local cache,
     * and return the updated list.
     */
    suspend fun sync(): Result<List<Category>>

    /** Create a new category on the server and insert it into the local cache. */
    suspend fun create(
        name: String,
        icon: String,
        type: String,
        isEncrypted: Boolean,
        encryptionSalt: String?,
        passwordHint: String?
    ): Result<Category>

    /** Update a category on the server and refresh the local cache entry. */
    suspend fun update(
        id: String,
        name: String,
        icon: String,
        passwordHint: String?
    ): Result<Category>

    /** Delete a category from the server and remove it from the local cache. */
    suspend fun delete(id: String): Result<Unit>

    suspend fun setFavorite(id: String, isFavorite: Boolean): Result<Unit>
}
