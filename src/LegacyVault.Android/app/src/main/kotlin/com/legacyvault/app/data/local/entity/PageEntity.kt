package com.legacyvault.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.model.enums.PageType

/**
 * Full page row.  [content] stores raw JSON for unencrypted pages and
 * Base64 ciphertext for encrypted ones — mirroring the wire format.
 * Decryption always happens in the use-case / presentation layer.
 */
@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity        = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns  = ["category_id"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("category_id"),
        Index("is_favorite"),
        Index("updated_at")
    ]
)
data class PageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "category_id")
    val categoryId: String,

    val type: String,           // PageType name
    val title: String,
    val content: String,        // raw JSON or Base64 ciphertext

    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean,

    @ColumnInfo(name = "encryption_salt")
    val encryptionSalt: String?,

    @ColumnInfo(name = "encryption_iv")
    val encryptionIV: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
) {
    fun toDomain(attachments: List<com.legacyvault.app.domain.model.Attachment> = emptyList()) = Page(
        id             = id,
        categoryId     = categoryId,
        type           = PageType.valueOf(type),
        title          = title,
        content        = content,
        isEncrypted    = isEncrypted,
        encryptionSalt = encryptionSalt,
        encryptionIV   = encryptionIV,
        isFavorite     = isFavorite,
        sortOrder      = sortOrder,
        createdAt      = createdAt,
        updatedAt      = updatedAt,
        attachments    = attachments
    )

    fun toSummary() = PageSummary(
        id          = id,
        categoryId  = categoryId,
        type        = PageType.valueOf(type),
        title       = title,
        isEncrypted = isEncrypted,
        isFavorite  = isFavorite,
        sortOrder   = sortOrder,
        updatedAt   = updatedAt
    )
}

fun Page.toEntity() = PageEntity(
    id             = id,
    categoryId     = categoryId,
    type           = type.name,
    title          = title,
    content        = content,
    isEncrypted    = isEncrypted,
    encryptionSalt = encryptionSalt,
    encryptionIV   = encryptionIV,
    isFavorite     = isFavorite,
    sortOrder      = sortOrder,
    createdAt      = createdAt,
    updatedAt      = updatedAt
)
