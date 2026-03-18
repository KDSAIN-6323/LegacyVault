package com.legacyvault.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Matches ReminderPageDto record from RemindersController.cs */
@Serializable
data class ReminderPageDto(
    val pageId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val title: String,
    val content: String         // Raw JSON — always unencrypted (server filters encrypted out)
)
