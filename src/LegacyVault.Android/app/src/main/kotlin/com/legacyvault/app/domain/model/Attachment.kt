package com.legacyvault.app.domain.model

data class Attachment(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val url: String
)
