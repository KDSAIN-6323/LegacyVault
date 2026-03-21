package com.legacyvault.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.local.entity.CategoryEntity
import com.legacyvault.app.data.local.entity.PageEntity

@Database(
    entities = [CategoryEntity::class, PageEntity::class],
    version  = 2,
    exportSchema = true
)
abstract class LegacyVaultDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun pageDao(): PageDao

    companion object {
        const val DATABASE_NAME = "legacyvault.db"
    }
}
