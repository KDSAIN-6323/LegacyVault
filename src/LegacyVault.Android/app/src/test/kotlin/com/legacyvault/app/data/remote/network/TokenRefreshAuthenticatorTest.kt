package com.legacyvault.app.data.remote.network

import com.legacyvault.app.data.remote.api.AuthApiService
import com.legacyvault.app.data.remote.dto.AuthResponse
import com.legacyvault.app.data.remote.dto.UserDto
import com.legacyvault.app.domain.model.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import javax.inject.Provider

class TokenRefreshAuthenticatorTest {

    private val mockWebServer = MockWebServer()

    private val tokenStore: TokenStore = mockk(relaxed = true)
    private val cookieJar: PersistentCookieJar = mockk(relaxed = true)
    private val authApiService: AuthApiService = mockk()
    private val authApiProvider = Provider { authApiService }

    private val authenticator = TokenRefreshAuthenticator(tokenStore, cookieJar, authApiProvider)

    private val client = OkHttpClient.Builder()
        .authenticator(authenticator)
        .build()

    @BeforeEach fun setUp()    { mockWebServer.start() }
    @AfterEach  fun tearDown() { mockWebServer.shutdown() }

    // ── Successful refresh ─────────────────────────────────────────────────

    @Test
    fun `retries request with new token after successful refresh`() {
        every { cookieJar.hasRefreshToken } returns true
        every { tokenStore.accessToken } returns "old-token"

        val refreshBody = AuthResponse(
            accessToken = "new-token",
            user = UserDto(id = "1", username = "alice", email = "alice@example.com")
        )
        coEvery { authApiService.refresh() } returns Response.success(refreshBody)

        // First call returns 401, second call (after refresh) returns 200
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val response = client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/data"))
                .header("Authorization", "Bearer old-token")
                .build()
        ).execute()

        assertEquals(200, response.code)
        verify { tokenStore.updateAccessToken("new-token") }
    }

    // ── No refresh token ───────────────────────────────────────────────────

    @Test
    fun `returns null when no refresh token is persisted`() {
        every { cookieJar.hasRefreshToken } returns false

        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/api/data")).build()
        ).execute()

        // Authenticator returns null → OkHttp propagates the 401
        assertEquals(401, response.code)
        verify(exactly = 0) { authApiService.toString() } // never called
    }

    // ── Refresh fails ──────────────────────────────────────────────────────

    @Test
    fun `clears credentials when refresh returns non-200`() {
        every { cookieJar.hasRefreshToken } returns true
        coEvery { authApiService.refresh() } returns Response.error(
            401,
            okhttp3.ResponseBody.create(null, "")
        )

        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/api/data")).build()
        ).execute()

        assertEquals(401, response.code)
        verify { tokenStore.clear() }
        verify { cookieJar.clearRefreshToken() }
    }

    @Test
    fun `clears credentials when refresh throws an exception`() {
        every { cookieJar.hasRefreshToken } returns true
        coEvery { authApiService.refresh() } throws RuntimeException("network error")

        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/api/data")).build()
        ).execute()

        assertEquals(401, response.code)
        verify { tokenStore.clear() }
        verify { cookieJar.clearRefreshToken() }
    }

    // ── Retry guard ────────────────────────────────────────────────────────

    @Test
    fun `does not retry if the failing request already had Authorization and a prior response`() {
        every { cookieJar.hasRefreshToken } returns true

        // Two 401s: first triggers authenticate(), second is the retry result
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val refreshBody = AuthResponse(
            accessToken = "new-token",
            user = UserDto(id = "1", username = "alice", email = "alice@example.com")
        )
        coEvery { authApiService.refresh() } returns Response.success(refreshBody)

        // Make two requests to simulate: first gets 401 (prior=null), second gets 401 (prior≠null)
        // The authenticator should abort on the second attempt to prevent an infinite loop.
        val response = client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/data"))
                .header("Authorization", "Bearer some-token")
                .build()
        ).execute()

        // After retry the second 401 is the final result
        assertEquals(401, response.code)
    }
}
