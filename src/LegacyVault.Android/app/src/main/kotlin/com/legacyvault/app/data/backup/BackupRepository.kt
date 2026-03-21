package com.legacyvault.app.data.backup

import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.dao.PageDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val pageDao: PageDao
) {
    private val json = Json {
        prettyPrint      = true
        ignoreUnknownKeys = true
        encodeDefaults   = true
    }

    /**
     * Serialises every category and page to a JSON string suitable for
     * writing to a file. Encrypted pages are included as ciphertext — they
     * can only be decrypted by someone who knows the vault password.
     */
    suspend fun exportJson(): String {
        val categories = categoryDao.getAll().map { it.toBackupDto() }
        val pages      = pageDao.getAll().map { it.toBackupDto() }
        val backup     = VaultBackup(
            exportedAt = Instant.now().toString(),
            categories = categories,
            pages      = pages
        )
        return json.encodeToString(backup)
    }

    /**
     * Parses [jsonString], upserts all categories and pages into Room,
     * then recalculates each category's `pageCount` from the live table.
     *
     * Duplicate IDs are replaced (REPLACE strategy), so re-importing the
     * same backup is idempotent.
     */
    suspend fun importJson(jsonString: String): Result<Unit> = runCatching {
        val backup = json.decodeFromString<VaultBackup>(jsonString)

        categoryDao.upsertAll(backup.categories.map { it.toEntity() })
        pageDao.upsertAll(backup.pages.map { it.toEntity() })

        // Re-derive page counts from the live page table
        backup.categories.forEach { cat ->
            categoryDao.recalculatePageCount(cat.id)
        }
    }
}
