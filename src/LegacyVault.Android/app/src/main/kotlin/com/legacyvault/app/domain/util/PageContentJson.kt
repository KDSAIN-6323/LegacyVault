package com.legacyvault.app.domain.util

import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.enums.PageType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Shared Json instance for [PageContent] serialization.
 * - ignoreUnknownKeys: safe to add new fields server-side without crashing
 * - isLenient: tolerates minor formatting quirks in legacy data
 * - encodeDefaults: ensures all fields are written (round-trip safe)
 */
val PageContentJson = Json {
    ignoreUnknownKeys = true
    isLenient         = true
    encodeDefaults    = true
}

/** Deserialize raw JSON content string → typed [PageContent] using the page's [PageType]. */
fun parsePageContent(json: String, type: PageType): PageContent =
    runCatching {
        when (type) {
            PageType.Note          -> PageContentJson.decodeFromString<PageContent.Note>(json)
            PageType.Password      -> PageContentJson.decodeFromString<PageContent.Password>(json)
            PageType.Recipe        -> PageContentJson.decodeFromString<PageContent.Recipe>(json)
            PageType.Quote         -> PageContentJson.decodeFromString<PageContent.Quote>(json)
            PageType.HomeInventory -> PageContentJson.decodeFromString<PageContent.HomeInventory>(json)
            PageType.Reminder      -> PageContentJson.decodeFromString<PageContent.Reminder>(json)
            PageType.ShoppingList  -> PageContentJson.decodeFromString<PageContent.ShoppingList>(json)
        }
    }.getOrElse { defaultContent(type) }

/** Serialize [PageContent] → raw JSON string for storage. */
fun serializePageContent(content: PageContent): String = when (content) {
    is PageContent.Note          -> PageContentJson.encodeToString(content)
    is PageContent.Password      -> PageContentJson.encodeToString(content)
    is PageContent.Recipe        -> PageContentJson.encodeToString(content)
    is PageContent.Quote         -> PageContentJson.encodeToString(content)
    is PageContent.HomeInventory -> PageContentJson.encodeToString(content)
    is PageContent.Reminder      -> PageContentJson.encodeToString(content)
    is PageContent.ShoppingList  -> PageContentJson.encodeToString(content)
}

/** Returns the default (empty) [PageContent] for a given [PageType]. */
fun defaultContent(type: PageType): PageContent = when (type) {
    PageType.Note          -> PageContent.Note()
    PageType.Password      -> PageContent.Password()
    PageType.Recipe        -> PageContent.Recipe()
    PageType.Quote         -> PageContent.Quote()
    PageType.HomeInventory -> PageContent.HomeInventory()
    PageType.Reminder      -> PageContent.Reminder()
    PageType.ShoppingList  -> PageContent.ShoppingList()
}
