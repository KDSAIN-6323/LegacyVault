Review a specific LegacyVault page type for consistency across all layers. The page type to review is: $ARGUMENTS

Read and cross-check all of the following for the named page type:

## 1. TypeScript types
- `src/LegacyVault.Web/src/types/index.ts` — does a `<Name>Content` interface exist with all expected fields?
- `src/LegacyVault.Mobile/src/types.ts` — does it match the web interface exactly?

## 2. C# model
- Find the relevant model/DTO in `src/LegacyVault.API/` — do the fields align with both TypeScript interfaces?

## 3. Android Kotlin model
- `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/domain/model/PageContent.kt` — does a `PageContent.<Name>` data class exist with matching fields (camelCase, same optionality)?
- `src/LegacyVault.Android/.../domain/model/enums/PageType.kt` — is the type present in the `PageType` enum?

## 4. Web editor
- `src/LegacyVault.Web/src/components/editors/<Name>Editor.tsx` — does it read/write all fields defined in the content interface? Are any fields missing from the UI?

## 5. Mobile detail view
- `src/LegacyVault.Mobile/app/app/category/[id]/page/[pageId].tsx` — does the render block for this type display all fields? Are any fields silently dropped?

## 6. Android editor screen
- `src/LegacyVault.Android/app/src/main/kotlin/com/legacyvault/app/ui/pages/editors/<Name>Editor.kt` — does the composable handle all fields from `PageContent.<Name>`? Does it correctly call `onContentChange` on every edit?

## 7. Calendar (Reminder only)
- If the type is `Reminder`, also check `src/LegacyVault.Web/src/components/calendar/CalendarView.tsx`:
  - Does it handle `endDate` (date range events)?
  - Does it handle `recurrenceInterval`?
  - Does it correctly skip encrypted reminders?

## 8. Notification service (Reminder only)
- If the type is `Reminder`, also check `src/LegacyVault.Web/src/services/notificationService.ts`:
  - Does `getNextOccurrence` use `recurrenceInterval`?
  - Does it skip past one-time events?

## Output

Report any field gaps, type mismatches, or missing UI coverage across all four layers (web, mobile, API, Android). If everything looks consistent, confirm that explicitly.
