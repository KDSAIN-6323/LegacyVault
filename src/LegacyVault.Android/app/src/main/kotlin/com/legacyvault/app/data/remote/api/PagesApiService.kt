package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.CreatePageRequest
import com.legacyvault.app.data.remote.dto.MovePageRequest
import com.legacyvault.app.data.remote.dto.PageDto
import com.legacyvault.app.data.remote.dto.PageSummaryDto
import com.legacyvault.app.data.remote.dto.UpdatePageRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PagesApiService {

    @GET("api/categories/{categoryId}/pages")
    suspend fun getByCategory(
        @Path("categoryId") categoryId: String
    ): Response<List<PageSummaryDto>>

    @GET("api/categories/{categoryId}/pages/{pageId}")
    suspend fun getById(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String
    ): Response<PageDto>

    @POST("api/categories/{categoryId}/pages")
    suspend fun create(
        @Path("categoryId") categoryId: String,
        @Body request: CreatePageRequest
    ): Response<PageDto>

    @PATCH("api/categories/{categoryId}/pages/{pageId}")
    suspend fun update(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String,
        @Body request: UpdatePageRequest
    ): Response<PageDto>

    @DELETE("api/categories/{categoryId}/pages/{pageId}")
    suspend fun delete(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String
    ): Response<Unit>

    @POST("api/categories/{categoryId}/pages/{pageId}/move")
    suspend fun move(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String,
        @Body request: MovePageRequest
    ): Response<PageDto>

    @POST("api/categories/{categoryId}/pages/{pageId}/favorite")
    suspend fun favorite(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String
    ): Response<Unit>

    @DELETE("api/categories/{categoryId}/pages/{pageId}/favorite")
    suspend fun unfavorite(
        @Path("categoryId") categoryId: String,
        @Path("pageId") pageId: String
    ): Response<Unit>
}
