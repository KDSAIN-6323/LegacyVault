package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Matches BackupEntryDto record from BackupController.cs */
@Serializable
data class BackupEntryDto(
    val fileName: String,
    val fileSizeBytes: Long,
    val createdAt: String
)

@Serializable
data class BackupPasswordRequest(
    val password: String
)
