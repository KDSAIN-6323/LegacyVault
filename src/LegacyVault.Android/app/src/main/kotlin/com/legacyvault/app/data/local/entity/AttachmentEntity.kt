package com.legacyvault.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.legacyvault.app.domain.model.Attachment

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity        = PageEntity::class,
            parentColumns = ["id"],
            childColumns  = ["page_id"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("page_id")]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "page_id")
    val pageId: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    val url: String
) {
    fun toDomain() = Attachment(
        id       = id,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = fileSize,
        url      = url
    )
}

fun Attachment.toEntity(pageId: String) = AttachmentEntity(
    id       = id,
    pageId   = pageId,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = fileSize,
    url      = url
)
