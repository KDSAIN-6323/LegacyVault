package com.legacyvault.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.legacyvault.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE page_id = :pageId")
    fun observeByPage(pageId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE page_id = :pageId")
    suspend fun getByPage(pageId: String): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM attachments WHERE page_id = :pageId")
    suspend fun deleteByPage(pageId: String)

    @Transaction
    suspend fun replaceByPageId(pageId: String, attachments: List<AttachmentEntity>) {
        deleteByPage(pageId)
        upsertAll(attachments)
    }
}
