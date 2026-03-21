package com.legacyvault.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MIGRATION_1_2
 *
 * This app was not in production at version 1. Version 1 was a dev-only schema.
 * Rather than guessing the exact v1 column set, we allow destructive migration
 * ONLY from version 1 via [provideDatabase]'s fallbackToDestructiveMigrationFrom(1).
 *
 * All migrations FROM version 2 onward must be written here explicitly.
 *
 * Example future migration template:
 *
 *   val MIGRATION_2_3 = object : Migration(2, 3) {
 *       override fun migrate(db: SupportSQLiteDatabase) {
 *           db.execSQL("ALTER TABLE pages ADD COLUMN tags TEXT")
 *       }
 *   }
 */
