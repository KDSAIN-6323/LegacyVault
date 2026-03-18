Compare the page content types across all four clients against the C# API models and report any fields that are out of sync.

## Files to read

1. `src/LegacyVault.Web/src/types/index.ts` — web TypeScript types
2. `src/LegacyVault.Mobile/src/types.ts` — mobile TypeScript types
3. All relevant C# model files in `src/LegacyVault.API/` (Models/, DTOs/, or similar)
4. `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/domain/model/PageContent.kt` — Android Kotlin sealed class
5. `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/domain/model/enums/` — Android enum types

## What to check

For each page content type (Note, Recipe, Quote, HomeInventory, Password, Reminder, ShoppingList, and any others found):
- Are all fields present in web types, mobile types, C# model, **and Android Kotlin model**?
- Are field names consistent (accounting for camelCase/PascalCase/snake_case conventions per platform)?
- Are optional (`?` / nullable `?`) markers consistent — if a field is optional in one client, is it optional in all?
- Are enum/union values consistent (e.g. `PageType` variants, `ReminderTag`, `ReminderRecurrence`, `NotifyUnit`)?

For `ReminderContent` specifically, verify `endDate?`, `recurrenceInterval?`, `notifyEnabled`, `notifyBefore`, `notifyUnit` are all present in web, mobile, C#, and Android.

For `ShoppingListItem`, verify the field is named `name` (not `text`) in all four clients.

## Output format

Report findings as a table or grouped list:

**In sync** — types that match across all four clients
**Discrepancies** — for each mismatch: which field, which file is missing or different, and the recommended fix

If everything is in sync, say so clearly.
