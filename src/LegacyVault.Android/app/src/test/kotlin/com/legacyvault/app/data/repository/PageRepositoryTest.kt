package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.AttachmentDao
import com.legacyvault.app.data.local.dao.PageDao
import com.legacyvault.app.data.local.entity.PageEntity
import com.legacyvault.app.data.remote.api.PagesApiService
import com.legacyvault.app.data.remote.dto.PageDto
import com.legacyvault.app.data.remote.dto.PageSummaryDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response

class PageRepositoryTest {

    private val pageDao: PageDao = mockk(relaxed = true)
    private val attachmentDao: AttachmentDao = mockk(relaxed = true)
    private val api: PagesApiService = mockk()
    private lateinit var repo: PageRepositoryImpl

    private val categoryId = "cat-1"
    private val pageId     = "page-1"

    private val testEntity = PageEntity(
        id             = pageId,
        categoryId     = categoryId,
        type           = "Note",
        title          = "My Note",
        content        = "{\"body\":\"hello\"}",
        isEncrypted    = false,
        encryptionSalt = null,
        encryptionIV   = null,
        isFavorite     = false,
        sortOrder      = 0,
        createdAt      = "2026-01-01T00:00:00Z",
        updatedAt      = "2026-01-02T00:00:00Z"
    )

    private val testSummaryDto = PageSummaryDto(
        id          = pageId,
        categoryId  = categoryId,
        type        = "Note",
        title       = "My Note",
        isEncrypted = false,
        isFavorite  = false,
        sortOrder   = 0,
        updatedAt   = "2026-01-02T00:00:00Z"
    )

    private val testPageDto = PageDto(
        id             = pageId,
        categoryId     = categoryId,
        type           = "Note",
        title          = "My Note",
        content        = "{\"body\":\"hello\"}",
        isEncrypted    = false,
        encryptionSalt = null,
        encryptionIV   = null,
        isFavorite     = false,
        sortOrder      = 0,
        createdAt      = "2026-01-01T00:00:00Z",
        updatedAt      = "2026-01-02T00:00:00Z",
        attachments    = emptyList()
    )

    @BeforeEach fun setUp() {
        repo = PageRepositoryImpl(pageDao, attachmentDao, api)
    }

    // ── observeByCategory ──────────────────────────────────────────────────

    @Test
    fun `observeByCategory maps entities to summaries`() = runTest {
        every { pageDao.observeByCategory(categoryId) } returns flowOf(listOf(testEntity))

        val results = repo.observeByCategory(categoryId).first()

        assertEquals(1, results.size)
        assertEquals(pageId, results[0].id)
        assertEquals("My Note", results[0].title)
    }

    // ── syncCategory ───────────────────────────────────────────────────────

    @Test
    fun `syncCategory fetches summaries, replaces cache, returns list`() = runTest {
        coEvery { api.getByCategory(categoryId) } returns Response.success(listOf(testSummaryDto))

        val result = repo.syncCategory(categoryId)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify { pageDao.replaceByCategoryId(categoryId, any()) }
    }

    @Test
    fun `syncCategory returns failure on API error`() = runTest {
        coEvery { api.getByCategory(categoryId) } throws RuntimeException("offline")

        val result = repo.syncCategory(categoryId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { pageDao.replaceByCategoryId(any(), any()) }
    }

    // ── fetchPage ──────────────────────────────────────────────────────────

    @Test
    fun `fetchPage caches page and attachments`() = runTest {
        coEvery { api.getById(categoryId, pageId) } returns Response.success(testPageDto)

        val result = repo.fetchPage(categoryId, pageId)

        assertTrue(result.isSuccess)
        assertEquals(pageId, result.getOrNull()?.id)
        coVerify { pageDao.upsert(any()) }
        coVerify { attachmentDao.replaceByPageId(pageId, emptyList()) }
    }

    // ── delete ─────────────────────────────────────────────────────────────

    @Test
    fun `delete removes page from cache after successful API call`() = runTest {
        coEvery { api.delete(categoryId, pageId) } returns Response.success(Unit)

        val result = repo.delete(categoryId, pageId)

        assertTrue(result.isSuccess)
        coVerify { pageDao.deleteById(pageId) }
    }

    @Test
    fun `delete keeps cache intact on API failure`() = runTest {
        coEvery { api.delete(categoryId, pageId) } returns Response.error(
            500,
            okhttp3.ResponseBody.create(null, "")
        )

        val result = repo.delete(categoryId, pageId)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { pageDao.deleteById(any()) }
    }

    // ── setFavorite ────────────────────────────────────────────────────────

    @Test
    fun `setFavorite true calls favorite API and updates cache`() = runTest {
        coEvery { api.favorite(categoryId, pageId) } returns Response.success(Unit)

        val result = repo.setFavorite(categoryId, pageId, true)

        assertTrue(result.isSuccess)
        coVerify { pageDao.setFavorite(pageId, true) }
    }

    // ── searchLocal ────────────────────────────────────────────────────────

    @Test
    fun `searchLocal returns summaries from DAO`() = runTest {
        coEvery { pageDao.searchByTitle("note") } returns listOf(testEntity)

        val results = repo.searchLocal("note")

        assertEquals(1, results.size)
        assertEquals("My Note", results[0].title)
    }

    @Test
    fun `searchLocal returns empty list when no matches`() = runTest {
        coEvery { pageDao.searchByTitle("xyz") } returns emptyList()

        val results = repo.searchLocal("xyz")

        assertTrue(results.isEmpty())
    }

    // ── move ───────────────────────────────────────────────────────────────

    @Test
    fun `move updates categoryId in cache after successful API call`() = runTest {
        val moved = testPageDto.copy(categoryId = "cat-2")
        coEvery { api.move(categoryId, pageId, any()) } returns Response.success(moved)

        val result = repo.move(categoryId, pageId, "cat-2")

        assertTrue(result.isSuccess)
        coVerify { pageDao.moveTo(pageId, "cat-2") }
    }
}
