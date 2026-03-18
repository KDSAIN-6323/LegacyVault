Audit the LegacyVault codebase to verify encrypted pages and vaults are properly gated across all clients.

## What to check

### Web (`src/LegacyVault.Web/`)

1. **Category list (Sidebar)** — encrypted categories should show a lock icon and require password unlock before pages are accessible
2. **Page list** — encrypted pages should not render content without a key in `keyCache`
3. **Page editors** — when saving an encrypted page, content must be encrypted via `cryptoService.encrypt` before dispatch
4. **Page display** — when loading an encrypted page, content must be decrypted via `cryptoService.decrypt` using the cached key
5. **keyCache usage** — verify `keyCache.set` is called on unlock and `keyCache.clear` is called on lock/logout
6. **Calendar** — confirm encrypted category pages are excluded from calendar reminder parsing (only non-encrypted reminders should show up)

### Mobile (`src/LegacyVault.Mobile/`)

1. **Category screen** (`app/app/category/[id].tsx`) — encrypted categories should prompt for password; key stored in `keyCache` after successful derivation
2. **Page detail screen** (`app/app/category/[id]/page/[pageId].tsx`) — if `page.isEncrypted`, content must be decrypted before rendering; handle decrypt failure gracefully
3. **keyCache** — verify the mobile `keyCache` is the same shape/API as the web one

### Android (`src/LegacyVault.Android/`)

1. **VaultUnlockScreen / VaultUnlockViewModel** — encrypted categories must route through `VaultUnlockScreen`; key must be stored in `KeyCache` after successful `CryptoService.deriveKey`
2. **Page detail** — if `page.isEncrypted`, content must be decrypted using the key from `KeyCache` before display; missing key should prompt re-unlock
3. **Page save** — when saving an encrypted page, content must be encrypted via `CryptoService.encrypt` with the cached key before calling the repository
4. **KeyCache** (`src/LegacyVault.Android/.../crypto/KeyCache.kt`) — verify `set` is called on vault unlock and `clear`/`clearAll` is called on vault lock, inactivity timeout, and logout
5. **InactivityManager** — verify `keyCache.clearAll()` is invoked before `logoutUseCase()` when the timer fires
6. **Wire format compatibility** — `CryptoServiceImpl.decrypt` must try mobile format `[tag‖ciphertext]` first, then fall back to web format `[ciphertext‖tag]`, so Android can read pages created by the web client

### API (`src/LegacyVault.API/`)

1. **Content storage** — confirm the API stores content as an opaque string (no server-side decrypt/re-encrypt)
2. **No plaintext leakage** — encrypted page content should never be logged or transformed server-side
3. **Reminders / Shopping endpoints** — confirm encrypted category pages are excluded from `/api/reminders` and `/api/shopping-lists` responses

## Output format

For each item above, report: ✅ correct / ⚠️ partial / ❌ missing or broken — with file and line references for anything that needs attention.
