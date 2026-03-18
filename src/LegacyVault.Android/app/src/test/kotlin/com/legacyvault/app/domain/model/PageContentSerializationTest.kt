package com.legacyvault.app.domain.model

import com.legacyvault.app.domain.model.enums.NotifyUnit
import com.legacyvault.app.domain.model.enums.ReminderRecurrence
import com.legacyvault.app.domain.model.enums.ReminderTag
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Verifies that each PageContent subtype serializes and deserializes correctly,
 * and that JSON produced here matches the wire format expected by the server
 * and existing web/mobile clients.
 */
class PageContentSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ── Note ──────────────────────────────────────────────────────────────

    @Test
    fun `Note round-trips correctly`() {
        val original = PageContent.Note(body = "Hello, world!")
        val serialized = json.encodeToString(PageContent.Note.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.Note.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun `Note deserializes from empty JSON`() {
        val note = json.decodeFromString(PageContent.Note.serializer(), "{}")
        assertEquals("", note.body)
    }

    // ── Password ──────────────────────────────────────────────────────────

    @Test
    fun `Password round-trips with all fields`() {
        val original = PageContent.Password(
            url = "https://example.com",
            username = "admin",
            password = "s3cr3t!",
            notes = "work account",
            totp = "JBSWY3DPEHPK3PXP"
        )
        val serialized = json.encodeToString(PageContent.Password.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.Password.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun `Password totp is null when absent`() {
        val pw = json.decodeFromString(
            PageContent.Password.serializer(),
            """{"url":"","username":"u","password":"p","notes":""}"""
        )
        assertNull(pw.totp)
    }

    // ── Recipe ────────────────────────────────────────────────────────────

    @Test
    fun `Recipe round-trips correctly`() {
        val original = PageContent.Recipe(
            ingredients = listOf("flour", "sugar", "eggs"),
            instructions = listOf("Mix", "Bake at 180°C for 30 min"),
            servings = 4,
            prepTime = "15 min",
            cookTime = "30 min",
            notes = "Grandma's recipe"
        )
        val serialized = json.encodeToString(PageContent.Recipe.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.Recipe.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    // ── Quote ─────────────────────────────────────────────────────────────

    @Test
    fun `Quote round-trips with tags`() {
        val original = PageContent.Quote(
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            source = "Stanford Commencement 2005",
            tags = listOf("inspiration", "work")
        )
        val serialized = json.encodeToString(PageContent.Quote.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.Quote.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    // ── HomeInventory ─────────────────────────────────────────────────────

    @Test
    fun `HomeInventory round-trips correctly`() {
        val original = PageContent.HomeInventory(
            itemName = "MacBook Pro",
            description = "16-inch M3 Max",
            location = "Office",
            value = 3499.99,
            purchaseDate = "2024-01-15",
            serialNumber = "C02XG1JHJGH5",
            warrantyExpiry = "2027-01-15",
            attachmentIds = listOf("att-1", "att-2")
        )
        val serialized = json.encodeToString(PageContent.HomeInventory.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.HomeInventory.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    // ── Reminder ─────────────────────────────────────────────────────────

    @Test
    fun `Reminder round-trips with all fields`() {
        val original = PageContent.Reminder(
            date = "2026-06-15",
            endDate = "2026-06-20",
            tag = ReminderTag.birthday,
            recurrence = ReminderRecurrence.yearly,
            recurrenceInterval = 1,
            notes = "Dad's birthday",
            notifyEnabled = true,
            notifyBefore = 3,
            notifyUnit = NotifyUnit.days
        )
        val serialized = json.encodeToString(PageContent.Reminder.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.Reminder.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun `Reminder serializes enum values as lowercase strings`() {
        val reminder = PageContent.Reminder(
            date = "2026-01-01",
            tag = ReminderTag.anniversary,
            recurrence = ReminderRecurrence.monthly,
            notifyUnit = NotifyUnit.weeks
        )
        val serialized = json.encodeToString(PageContent.Reminder.serializer(), reminder)
        assert(serialized.contains("\"anniversary\"")) { "tag should be lowercase: $serialized" }
        assert(serialized.contains("\"monthly\""))     { "recurrence should be lowercase: $serialized" }
        assert(serialized.contains("\"weeks\""))       { "notifyUnit should be lowercase: $serialized" }
    }

    // ── ShoppingList ──────────────────────────────────────────────────────

    @Test
    fun `ShoppingList round-trips with items`() {
        val original = PageContent.ShoppingList(
            items = listOf(
                ShoppingListItem(id = "1", name = "Milk", quantity = "2L", checked = false),
                ShoppingListItem(id = "2", name = "Eggs", quantity = "12", checked = true)
            ),
            notes = "Weekly shop"
        )
        val serialized = json.encodeToString(PageContent.ShoppingList.serializer(), original)
        val deserialized = json.decodeFromString(PageContent.ShoppingList.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun `ShoppingListItem uses name field not text (web client compatibility)`() {
        val json2 = json.encodeToString(
            ShoppingListItem.serializer(),
            ShoppingListItem(id = "x", name = "Bread", quantity = "1", checked = false)
        )
        assert(json2.contains("\"name\"")) { "Must use 'name' field, not 'text': $json2" }
        assert(!json2.contains("\"text\"")) { "Must not use 'text' field: $json2" }
    }
}
