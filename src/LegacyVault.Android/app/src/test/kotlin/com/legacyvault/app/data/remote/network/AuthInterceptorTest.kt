package com.legacyvault.app.data.remote.network

import com.legacyvault.app.domain.model.User
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthInterceptorTest {

    private val mockWebServer = MockWebServer()
    private val tokenStore: TokenStore = mockk(relaxed = true)
    private val interceptor = AuthInterceptor(tokenStore)

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    @BeforeEach fun setUp()    { mockWebServer.start() }
    @AfterEach  fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun `adds Bearer token when accessToken is present`() {
        every { tokenStore.accessToken } returns "my-jwt"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder().url(mockWebServer.url("/api/test")).build()
        ).execute()

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer my-jwt", recorded.getHeader("Authorization"))
    }

    @Test
    fun `does not add Authorization header when no token`() {
        every { tokenStore.accessToken } returns null
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder().url(mockWebServer.url("/api/test")).build()
        ).execute()

        val recorded = mockWebServer.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }

    @Test
    fun `does not overwrite an existing Authorization header`() {
        every { tokenStore.accessToken } returns "store-token"
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        client.newCall(
            Request.Builder()
                .url(mockWebServer.url("/api/auth/login"))
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .build()
        ).execute()

        val recorded = mockWebServer.takeRequest()
        assertEquals("Basic dXNlcjpwYXNz", recorded.getHeader("Authorization"))
    }

    @Test
    fun `request proceeds even with empty token store`() {
        every { tokenStore.accessToken } returns null
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/api/protected")).build()
        ).execute()

        assertEquals(401, response.code)
    }
}
