package com.legacyvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.legacyvault.app.data.local.dao.AttachmentDao
import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.local.entity.AttachmentEntity
import com.legacyvault.app.data.local.entity.CategoryEntity
import com.legacyvault.app.data.local.entity.PageEntity

@Database(
    entities = [
        CategoryEntity::class,
        PageEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = true     // keeps a schema history for migration auditing
)
abstract class LegacyVaultDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun pageDao(): PageDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        const val DATABASE_NAME = "legacyvault.db"
    }
}
