package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.NotifyUnit
import com.legacyvault.app.domain.model.enums.ReminderRecurrence
import com.legacyvault.app.domain.model.enums.ReminderTag
import kotlinx.serialization.Serializable

/**
 * Typed representations of [Page.content] once deserialized from JSON.
 *
 * The server stores content as a raw JSON string. For unencrypted pages it is
 * deserialized directly; for encrypted pages the JSON is decrypted first, then
 * deserialized. Field names match the TypeScript interfaces in types/index.ts
 * exactly so that existing encrypted data round-trips correctly.
 *
 * Default values on all fields ensure that JSON with missing keys (e.g. older
 * data) deserializes cleanly without exceptions.
 */
sealed class PageContent {

    @Serializable
    data class Note(
        val body: String = ""
    ) : PageContent()

    @Serializable
    data class Password(
        val url: String = "",
        val username: String = "",
        val password: String = "",
        val notes: String = "",
        val totp: String? = null
    ) : PageContent()

    @Serializable
    data class Recipe(
        val ingredients: List<String> = emptyList(),
        val instructions: List<String> = emptyList(),
        val servings: Int = 0,
        val prepTime: String = "",
        val cookTime: String = "",
        val notes: String = ""
    ) : PageContent()

    @Serializable
    data class Quote(
        val text: String = "",
        val author: String = "",
        val source: String = "",
        val tags: List<String> = emptyList()
    ) : PageContent()

    @Serializable
    data class HomeInventory(
        val itemName: String = "",
        val description: String = "",
        val location: String = "",
        val value: Double = 0.0,
        val purchaseDate: String = "",
        val serialNumber: String = "",
        val warrantyExpiry: String = "",
        val attachmentIds: List<String> = emptyList()
    ) : PageContent()

    @Serializable
    data class Reminder(
        val date: String = "",
        val endDate: String? = null,
        val tag: ReminderTag = ReminderTag.custom,
        val recurrence: ReminderRecurrence = ReminderRecurrence.once,
        val recurrenceInterval: Int = 1,
        val notes: String = "",
        val notifyEnabled: Boolean = false,
        val notifyBefore: Int = 1,
        val notifyUnit: NotifyUnit = NotifyUnit.days
    ) : PageContent()

    @Serializable
    data class ShoppingList(
        val items: List<ShoppingListItem> = emptyList(),
        val notes: String = ""
    ) : PageContent()
}

/**
 * A single item in a [PageContent.ShoppingList].
 * Field names match the web client: id / name / quantity / checked.
 * (The mobile Expo client uses "text" instead of "name" — the web shape is canonical.)
 */
@Serializable
data class ShoppingListItem(
    val id: String = "",
    val name: String = "",
    val quantity: String = "",
    val checked: Boolean = false
)
