package com.legacyvault.app.domain.repository

import com.legacyvault.app.domain.model.Attachment
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface AttachmentRepository {

    fun observeByPage(pageId: String): Flow<List<Attachment>>

    suspend fun upload(
        categoryId: String,
        pageId: String,
        file: MultipartBody.Part
    ): Result<Attachment>

    suspend fun delete(
        categoryId: String,
        pageId: String,
        attachmentId: String
    ): Result<Unit>
}
