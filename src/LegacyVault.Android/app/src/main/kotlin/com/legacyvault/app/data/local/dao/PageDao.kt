package com.legacyvault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.legacyvault.app.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("""
        SELECT * FROM pages
        WHERE category_id = :categoryId
        ORDER BY sort_order ASC, updated_at DESC
    """)
    fun observeByCategory(categoryId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :id")
    fun observeById(id: String): Flow<PageEntity?>

    @Query("SELECT * FROM pages WHERE id = :id")
    suspend fun getById(id: String): PageEntity?

    @Query("""
        SELECT * FROM pages
        WHERE is_favorite = 1
        ORDER BY updated_at DESC
    """)
    fun observeFavorites(): Flow<List<PageEntity>>

    @Query("""
        SELECT * FROM pages
        WHERE type = :type
        ORDER BY title ASC, updated_at DESC
    """)
    fun observeByType(type: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages ORDER BY created_at ASC")
    suspend fun getAll(): List<PageEntity>

    /** Full-text search across title. Case-insensitive LIKE. */
    @Query("""
        SELECT * FROM pages
        WHERE title LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
        LIMIT 50
    """)
    suspend fun searchByTitle(query: String): List<PageEntity>

    // ── Upsert / insert ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(pages: List<PageEntity>)

    // ── Updates ────────────────────────────────────────────────────────────

    @Query("UPDATE pages SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE pages SET sort_order = :sortOrder WHERE id = :id")
    suspend fun setSortOrder(id: String, sortOrder: Int)

    @Query("UPDATE pages SET category_id = :targetCategoryId WHERE id = :id")
    suspend fun moveTo(id: String, targetCategoryId: String)

    // ── Delete ─────────────────────────────────────────────────────────────

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pages WHERE category_id = :categoryId")
    suspend fun deleteByCategory(categoryId: String)

    /**
     * Replace all cached pages for a given category with a fresh server snapshot.
     * Runs in a transaction so the UI never sees a momentarily empty list.
     */
    @Transaction
    suspend fun replaceByCategoryId(categoryId: String, pages: List<PageEntity>) {
        deleteByCategory(categoryId)
        upsertAll(pages)
    }

    @Query("DELETE FROM pages")
    suspend fun deleteAll()
}
