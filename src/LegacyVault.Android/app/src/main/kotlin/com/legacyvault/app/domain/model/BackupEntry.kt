package com.legacyvault.app.domain.model

data class BackupEntry(
    val fileName: String,
    val fileSizeBytes: Long,
    val createdAt: String
)
