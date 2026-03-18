package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.CategoryDao
import com.legacyvault.app.data.local.entity.CategoryEntity
import com.legacyvault.app.data.remote.api.CategoriesApiService
import com.legacyvault.app.data.remote.dto.CategoryDto
import com.legacyvault.app.domain.model.enums.CategoryType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class CategoryRepositoryTest {

    private val dao: CategoryDao = mockk(relaxed = true)
    private val api: CategoriesApiService = mockk()
    private lateinit var repo: CategoryRepositoryImpl

    private val testEntity = CategoryEntity(
        id             = "cat-1",
        type           = "General",
        name           = "Personal",
        icon           = "📁",
        isEncrypted    = false,
        encryptionSalt = null,
        passwordHint   = null,
        isFavorite     = false,
        pageCount      = 3,
        createdAt      = "2026-01-01T00:00:00Z",
        updatedAt      = "2026-01-02T00:00:00Z"
    )

    private val testDto = CategoryDto(
        id             = "cat-1",
        type           = "General",
        name           = "Personal",
        icon           = "📁",
        isEncrypted    = false,
        encryptionSalt = null,
        passwordHint   = null,
        isFavorite     = false,
        pageCount      = 3,
        createdAt      = "2026-01-01T00:00:00Z",
        updatedAt      = "2026-01-02T00:00:00Z"
    )

    @BeforeEach fun setUp() {
        repo = CategoryRepositoryImpl(dao, api)
    }

    // ── observeAll ─────────────────────────────────────────────────────────

    @Test
    fun `observeAll maps entities to domain models`() = runTest {
        every { dao.observeAll() } returns flowOf(listOf(testEntity))

        val result = repo.observeAll().first()

        assertEquals(1, result.size)
        assertEquals("cat-1", result[0].id)
        assertEquals(CategoryType.General, result[0].type)
        assertEquals("Personal", result[0].name)
    }

    @Test
    fun `observeAll returns empty list when cache is empty`() = runTest {
        every { dao.observeAll() } returns flowOf(emptyList())
        val result = repo.observeAll().first()
        assertTrue(result.isEmpty())
    }

    // ── observeById ────────────────────────────────────────────────────────

    @Test
    fun `observeById maps entity to domain`() = runTest {
        every { dao.observeById("cat-1") } returns flowOf(testEntity)
        val result = repo.observeById("cat-1").first()
        assertEquals("cat-1", result?.id)
    }

    @Test
    fun `observeById emits null when entity not found`() = runTest {
        every { dao.observeById("missing") } returns flowOf(null)
        val result = repo.observeById("missing").first()
        assertEquals(null, result)
    }

    // ── sync ───────────────────────────────────────────────────────────────

    @Test
    fun `sync fetches from API, replaces cache, returns domain list`() = runTest {
        coEvery { api.getAll() } returns Response.success(listOf(testDto))

        val result = repo.sync()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("cat-1", result.getOrNull()?.first()?.id)
        coVerify { dao.replaceAll(any()) }
    }

    @Test
    fun `sync returns failure when API throws`() = runTest {
        coEvery { api.getAll() } throws RuntimeException("network error")

        val result = repo.sync()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.replaceAll(any()) }
    }

    @Test
    fun `sync returns failure on non-2xx response`() = runTest {
        coEvery { api.getAll() } returns Response.error(
            500,
            okhttp3.ResponseBody.create(null, "")
        )

        val result = repo.sync()

        assertTrue(result.isFailure)
    }

    // ── delete ─────────────────────────────────────────────────────────────

    @Test
    fun `delete removes from cache after successful API call`() = runTest {
        coEvery { api.delete("cat-1") } returns Response.success(Unit)

        val result = repo.delete("cat-1")

        assertTrue(result.isSuccess)
        coVerify { dao.deleteById("cat-1") }
    }

    @Test
    fun `delete returns failure and does not touch cache on API error`() = runTest {
        coEvery { api.delete("cat-1") } returns Response.error(
            404,
            okhttp3.ResponseBody.create(null, "")
        )

        val result = repo.delete("cat-1")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    // ── setFavorite ────────────────────────────────────────────────────────

    @Test
    fun `setFavorite true calls favorite endpoint and updates cache`() = runTest {
        coEvery { api.favorite("cat-1") } returns Response.success(Unit)

        val result = repo.setFavorite("cat-1", true)

        assertTrue(result.isSuccess)
        coVerify { dao.setFavorite("cat-1", true) }
    }

    @Test
    fun `setFavorite false calls unfavorite endpoint and updates cache`() = runTest {
        coEvery { api.unfavorite("cat-1") } returns Response.success(Unit)

        val result = repo.setFavorite("cat-1", false)

        assertTrue(result.isSuccess)
        coVerify { dao.setFavorite("cat-1", false) }
    }
}
