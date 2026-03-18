package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.BackupEntryDto
import com.legacyvault.app.data.remote.dto.BackupPasswordRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface BackupApiService {

    @GET("api/backup")
    suspend fun list(): Response<List<BackupEntryDto>>

    /** Trigger a new backup. The server stores the file server-side. */
    @POST("api/backup")
    suspend fun create(@Body request: BackupPasswordRequest): Response<BackupEntryDto>

    /** Download a specific backup file as a binary stream. */
    @Streaming
    @GET("api/backup/{fileName}/download")
    suspend fun download(
        @Path("fileName") fileName: String
    ): Response<ResponseBody>

    @DELETE("api/backup/{fileName}")
    suspend fun delete(
        @Path("fileName") fileName: String
    ): Response<Unit>
}
