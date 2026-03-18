package com.legacyvault.app.data.repository

import com.legacyvault.app.data.local.dao.AttachmentDao
import com.legacyvault.app.data.local.entity.toEntity
import com.legacyvault.app.data.remote.api.AttachmentsApiService
import com.legacyvault.app.data.remote.mapper.toDomain
import com.legacyvault.app.data.remote.network.bodyOrThrow
import com.legacyvault.app.data.remote.network.throwIfError
import com.legacyvault.app.domain.model.Attachment
import com.legacyvault.app.domain.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentRepositoryImpl @Inject constructor(
    private val dao: AttachmentDao,
    private val api: AttachmentsApiService
) : AttachmentRepository {

    override fun observeByPage(pageId: String): Flow<List<Attachment>> =
        dao.observeByPage(pageId).map { it.map { e -> e.toDomain() } }

    override suspend fun upload(
        categoryId: String,
        pageId: String,
        file: MultipartBody.Part
    ): Result<Attachment> = runCatching {
        val attachment = api.upload(categoryId, pageId, file).bodyOrThrow().toDomain()
        dao.upsert(attachment.toEntity(pageId))
        attachment
    }

    override suspend fun delete(
        categoryId: String,
        pageId: String,
        attachmentId: String
    ): Result<Unit> = runCatching {
        api.delete(categoryId, pageId, attachmentId).throwIfError()
        dao.deleteById(attachmentId)
    }
}
