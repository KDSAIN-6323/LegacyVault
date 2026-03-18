Compare the TypeScript types across the web and mobile projects against the C# API models and report any fields that are out of sync.

## Files to read
1. `src/LegacyVault.Web/src/types/index.ts` — web TypeScript types
2. `src/LegacyVault.Mobile/src/types.ts` — mobile TypeScript types
3. All relevant C# model files in `src/LegacyVault.API/` (Models/, DTOs/, or similar)

## What to check

For each page content type (Note, Recipe, Quote, HomeInventory, Password, Reminder, ShoppingList, and any others found):
- Are all fields present in web types, mobile types, and the C# model?
- Are field names consistent (accounting for camelCase vs PascalCase)?
- Are optional (`?`) markers consistent — if a field is optional in one, is it optional in all three?
- Are enum/union values consistent (e.g. `PageType` variants)?

For `ReminderContent` specifically, verify `endDate?`, `recurrenceInterval?`, `notifyEnabled`, `notifyBefore`, `notifyUnit` are all present in both web and mobile.

## Output format

Report findings as a table or grouped list:

**In sync** — types that match across all three
**Discrepancies** — for each mismatch: which field, which file is missing or different, and the recommended fix

If everything is in sync, say so clearly.
