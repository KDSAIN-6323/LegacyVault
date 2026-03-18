package com.legacyvault.app.data.remote.mapper

import com.legacyvault.app.data.remote.dto.CategoryDto
import com.legacyvault.app.data.remote.dto.PageDto
import com.legacyvault.app.data.remote.dto.SearchResultDto
import com.legacyvault.app.domain.model.enums.CategoryType
import com.legacyvault.app.domain.model.enums.PageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DomainMappersTest {

    @Test
    fun `CategoryDto maps General type correctly`() {
        val dto = CategoryDto(
            id = "cat-1", type = "General", name = "Notes", icon = "📝",
            isEncrypted = false, encryptionSalt = null, passwordHint = null,
            isFavorite = false, pageCount = 3,
            createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z"
        )
        val domain = dto.toDomain()
        assertEquals(CategoryType.General, domain.type)
        assertFalse(domain.isVault)
        assertEquals(3, domain.pageCount)
    }

    @Test
    fun `CategoryDto maps Vault type correctly`() {
        val dto = CategoryDto(
            id = "cat-2", type = "Vault", name = "Passwords", icon = "🔑",
            isEncrypted = true, encryptionSalt = "abc123==", passwordHint = "pet name",
            isFavorite = true, pageCount = 10,
            createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-02T00:00:00Z"
        )
        val domain = dto.toDomain()
        assertEquals(CategoryType.Vault, domain.type)
        assertTrue(domain.isVault)
        assertEquals("abc123==", domain.encryptionSalt)
        assertEquals("pet name", domain.passwordHint)
    }

    @Test
    fun `PageDto maps all PageType values`() {
        val types = listOf("Note", "Recipe", "Quote", "HomeInventory", "Password", "Reminder", "ShoppingList")
        types.forEach { typeName ->
            val dto = minimalPageDto(type = typeName)
            val domain = dto.toDomain()
            assertEquals(PageType.valueOf(typeName), domain.type, "Failed for type: $typeName")
        }
    }

    @Test
    fun `PageDto content field is preserved verbatim`() {
        val rawContent = """{"body":"test note content"}"""
        val dto = minimalPageDto(content = rawContent)
        assertEquals(rawContent, dto.toDomain().content)
    }

    @Test
    fun `SearchResultDto maps type string to PageType enum`() {
        val dto = SearchResultDto(
            pageId = "p1", categoryId = "c1", categoryName = "Notes", categoryIcon = "📝",
            type = "Note", title = "My Note", isEncrypted = false,
            updatedAt = "2026-01-01T00:00:00Z"
        )
        assertEquals(PageType.Note, dto.toDomain().type)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun minimalPageDto(
        type: String = "Note",
        content: String = "{}"
    ) = PageDto(
        id = "page-1", categoryId = "cat-1", type = type,
        title = "Test", content = content,
        isEncrypted = false, encryptionSalt = null, encryptionIV = null,
        isFavorite = false, sortOrder = 0,
        createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z",
        attachments = emptyList()
    )
}
