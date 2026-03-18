package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.AttachmentDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface AttachmentsApiService {

    @GET("api/categories/{categoryId}/pages/{pageId}/attachments")
    suspend fun getAll(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String
    ): Response<List<AttachmentDto>>

    @Multipart
    @POST("api/categories/{categoryId}/pages/{pageId}/attachments")
    suspend fun upload(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String,
        @Part file: MultipartBody.Part
    ): Response<AttachmentDto>

    @Streaming
    @GET("api/categories/{categoryId}/pages/{pageId}/attachments/{attachmentId}/download")
    suspend fun download(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String,
        @Path("attachmentId") attachmentId: String
    ): Response<ResponseBody>

    @DELETE("api/categories/{categoryId}/pages/{pageId}/attachments/{attachmentId}")
    suspend fun delete(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String,
        @Path("attachmentId") attachmentId: String
    ): Response<Unit>
}
