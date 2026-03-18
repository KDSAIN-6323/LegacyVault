package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.AuthResponse
import com.legacyvault.app.data.remote.dto.LoginRequest
import com.legacyvault.app.data.remote.dto.RegisterRequest
import com.legacyvault.app.data.remote.dto.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * Silent token refresh.  The server reads the refresh token from the
     * httpOnly "refreshToken" cookie — no body needed.
     * Handled by [PersistentCookieJar] automatically.
     */
    @POST("api/auth/refresh")
    suspend fun refresh(): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    /**
     * Required by Google Play Store Data Safety policy.
     * Permanently deletes the authenticated user's account and all data.
     */
    @DELETE("api/auth/account")
    suspend fun deleteAccount(): Response<Unit>

    @GET("api/health")
    suspend fun health(): Response<com.legacyvault.app.data.remote.dto.HealthResponse>
}
