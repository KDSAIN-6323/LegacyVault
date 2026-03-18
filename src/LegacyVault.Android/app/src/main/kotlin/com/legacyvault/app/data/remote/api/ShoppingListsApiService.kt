package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.ShoppingListRefDto
import retrofit2.Response
import retrofit2.http.GET

interface ShoppingListsApiService {

    /**
     * Returns references to all ShoppingList pages for the current user.
     * Only non-encrypted lists are included (server omits encrypted ones).
     * Use [PagesApiService.getById] to load the full content.
     */
    @GET("api/shopping-lists")
    suspend fun getAll(): Response<List<ShoppingListRefDto>>
}
