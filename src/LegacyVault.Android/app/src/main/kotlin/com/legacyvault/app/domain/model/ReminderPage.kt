package com.legacyvault.app.domain.model

/**
 * Returned by GET /api/reminders — only unencrypted Reminder pages.
 * [content] is raw JSON matching [PageContent.Reminder].
 */
data class ReminderPage(
    val pageId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val title: String,
    val content: String
)
