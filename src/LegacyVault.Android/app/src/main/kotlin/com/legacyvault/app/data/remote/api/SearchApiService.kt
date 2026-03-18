package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.SearchResultDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("api/search")
    suspend fun search(
        @Query("q") query: String
    ): Response<List<SearchResultDto>>
}
