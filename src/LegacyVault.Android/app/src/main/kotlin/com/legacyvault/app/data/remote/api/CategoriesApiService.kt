package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.CategoryDto
import com.legacyvault.app.data.remote.dto.CreateCategoryRequest
import com.legacyvault.app.data.remote.dto.UpdateCategoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoriesApiService {

    @GET("api/categories")
    suspend fun getAll(): Response<List<CategoryDto>>

    @GET("api/categories/{id}")
    suspend fun getById(@Path("id") id: String): Response<CategoryDto>

    @POST("api/categories")
    suspend fun create(@Body request: CreateCategoryRequest): Response<CategoryDto>

    @PATCH("api/categories/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): Response<CategoryDto>

    @DELETE("api/categories/{id}")
    suspend fun delete(@Path("id") id: String): Response<Unit>

    @POST("api/categories/{id}/favorite")
    suspend fun favorite(@Path("id") id: String): Response<Unit>

    @DELETE("api/categories/{id}/favorite")
    suspend fun unfavorite(@Path("id") id: String): Response<Unit>
}
