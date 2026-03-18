package com.legacyvault.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.enums.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,

    val type: String,           // CategoryType name
    val name: String,
    val icon: String,

    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean,

    @ColumnInfo(name = "encryption_salt")
    val encryptionSalt: String?,

    @ColumnInfo(name = "password_hint")
    val passwordHint: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "page_count")
    val pageCount: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
) {
    fun toDomain() = Category(
        id             = id,
        type           = CategoryType.valueOf(type),
        name           = name,
        icon           = icon,
        isEncrypted    = isEncrypted,
        encryptionSalt = encryptionSalt,
        passwordHint   = passwordHint,
        isFavorite     = isFavorite,
        pageCount      = pageCount,
        createdAt      = createdAt,
        updatedAt      = updatedAt
    )
}

fun Category.toEntity() = CategoryEntity(
    id             = id,
    type           = type.name,
    name           = name,
    icon           = icon,
    isEncrypted    = isEncrypted,
    encryptionSalt = encryptionSalt,
    passwordHint   = passwordHint,
    isFavorite     = isFavorite,
    pageCount      = pageCount,
    createdAt      = createdAt,
    updatedAt      = updatedAt
)
