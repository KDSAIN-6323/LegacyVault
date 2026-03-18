package com.legacyvault.app.data.remote.mapper

import com.legacyvault.app.data.remote.dto.AttachmentDto
import com.legacyvault.app.data.remote.dto.AuthResponse
import com.legacyvault.app.data.remote.dto.BackupEntryDto
import com.legacyvault.app.data.remote.dto.CategoryDto
import com.legacyvault.app.data.remote.dto.PageDto
import com.legacyvault.app.data.remote.dto.PageSummaryDto
import com.legacyvault.app.data.remote.dto.ReminderPageDto
import com.legacyvault.app.data.remote.dto.SearchResultDto
import com.legacyvault.app.data.remote.dto.ShoppingListRefDto
import com.legacyvault.app.data.remote.dto.UserDto
import com.legacyvault.app.domain.model.Attachment
import com.legacyvault.app.domain.model.BackupEntry
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.Page
import com.legacyvault.app.domain.model.PageSummary
import com.legacyvault.app.domain.model.ReminderPage
import com.legacyvault.app.domain.model.SearchResult
import com.legacyvault.app.domain.model.ShoppingListRef
import com.legacyvault.app.domain.model.User
import com.legacyvault.app.domain.model.enums.CategoryType
import com.legacyvault.app.domain.model.enums.PageType

fun UserDto.toDomain() = User(id = id, username = username, email = email)

fun AuthResponse.toDomainUser() = user.toDomain()

fun CategoryDto.toDomain() = Category(
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

fun PageSummaryDto.toDomain() = PageSummary(
    id          = id,
    categoryId  = categoryId,
    type        = PageType.valueOf(type),
    title       = title,
    isEncrypted = isEncrypted,
    isFavorite  = isFavorite,
    sortOrder   = sortOrder,
    updatedAt   = updatedAt
)

fun PageDto.toDomain() = Page(
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
    attachments    = attachments.map { it.toDomain() }
)

fun AttachmentDto.toDomain() = Attachment(
    id       = id,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = fileSize,
    url      = url
)

fun SearchResultDto.toDomain() = SearchResult(
    pageId       = pageId,
    categoryId   = categoryId,
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    type         = PageType.valueOf(type),
    title        = title,
    isEncrypted  = isEncrypted,
    updatedAt    = updatedAt
)

fun ReminderPageDto.toDomain() = ReminderPage(
    pageId       = pageId,
    categoryId   = categoryId,
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    title        = title,
    content      = content
)

fun ShoppingListRefDto.toDomain() = ShoppingListRef(
    id          = id,
    categoryId  = categoryId,
    title       = title,
    isEncrypted = isEncrypted
)

fun BackupEntryDto.toDomain() = BackupEntry(
    fileName      = fileName,
    fileSizeBytes = fileSizeBytes,
    createdAt     = createdAt
)
