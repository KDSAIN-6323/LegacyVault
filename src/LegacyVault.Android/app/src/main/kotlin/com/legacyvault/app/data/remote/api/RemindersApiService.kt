package com.legacyvault.app.data.remote.api

import com.legacyvault.app.data.remote.dto.ReminderPageDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RemindersApiService {

    /**
     * Returns upcoming / overdue reminder pages for the authenticated user.
     * The server filters out encrypted reminder pages (content not readable server-side).
     *
     * @param daysAhead optional lookahead window; defaults to server-configured value
     */
    @GET("api/reminders")
    suspend fun getUpcoming(
        @Query("daysAhead") daysAhead: Int? = null
    ): Response<List<ReminderPageDto>>
}
