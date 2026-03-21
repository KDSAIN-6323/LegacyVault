package com.legacyvault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.legacyvault.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM categories ORDER BY is_favorite DESC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: String): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE is_favorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY created_at ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("UPDATE categories SET page_count = (SELECT COUNT(*) FROM pages WHERE category_id = :id) WHERE id = :id")
    suspend fun recalculatePageCount(id: String)

    // ── Upsert / insert ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    // ── Updates ────────────────────────────────────────────────────────────

    @Query("UPDATE categories SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE categories SET page_count = page_count + 1 WHERE id = :id")
    suspend fun incrementPageCount(id: String)

    @Query("UPDATE categories SET page_count = MAX(0, page_count - 1) WHERE id = :id")
    suspend fun decrementPageCount(id: String)

    // ── Delete ─────────────────────────────────────────────────────────────

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Replace the entire cached category list with a fresh server snapshot.
     * Called during a full sync; runs inside a transaction so the UI never
     * sees an empty list mid-swap.
     */
    @Transaction
    suspend fun replaceAll(categories: List<CategoryEntity>) {
        deleteAll()
        upsertAll(categories)
    }

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
