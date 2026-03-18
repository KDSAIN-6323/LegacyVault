Review a specific LegacyVault page type for consistency across all layers. The page type to review is: $ARGUMENTS

Read and cross-check all of the following for the named page type:

## 1. TypeScript types
- `src/LegacyVault.Web/src/types/index.ts` — does a `<Name>Content` interface exist with all expected fields?
- `src/LegacyVault.Mobile/src/types.ts` — does it match the web interface exactly?

## 2. C# model
- Find the relevant model/DTO in `src/LegacyVault.API/` — do the fields align with both TypeScript interfaces?

## 3. Web editor
- `src/LegacyVault.Web/src/components/editors/<Name>Editor.tsx` — does it read/write all fields defined in the content interface? Are any fields missing from the UI?

## 4. Mobile detail view
- `src/LegacyVault.Mobile/app/app/category/[id]/page/[pageId].tsx` — does the render block for this type display all fields? Are any fields silently dropped?

## 5. Calendar (Reminder only)
- If the type is `Reminder`, also check `src/LegacyVault.Web/src/components/calendar/CalendarView.tsx`:
  - Does it handle `endDate` (date range events)?
  - Does it handle `recurrenceInterval`?
  - Does it correctly skip encrypted reminders?

## 6. Notification service (Reminder only)
- If the type is `Reminder`, also check `src/LegacyVault.Web/src/services/notificationService.ts`:
  - Does `getNextOccurrence` use `recurrenceInterval`?
  - Does it skip past one-time events?

## Output

Report any field gaps, type mismatches, or missing UI coverage. If everything looks consistent, confirm that explicitly.
