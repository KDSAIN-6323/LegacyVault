# Changelog

All notable changes to Legacy Vault will be documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — 2026-03-20

### Added — Android (`LegacyVault.Android`)

**Foundation (Phase 1)**
- Native Kotlin/Jetpack Compose Android app targeting API 26–35 (Play Store ready)
- Material 3 theming with brand colors matching the web client (`#2563EB` light, `#89B4FA` dark)
- Full navigation graph with all routes stubbed (Auth, Categories, Pages, Search, Settings, Backup)
- App ID corrected from `com.legayvault.legacyvault` → `com.legacyvault.app`
- Network security config: HTTPS-only in release, localhost HTTP allowed in debug
- Hilt dependency injection wired across Application and all entry points

**Domain & type system (Phase 2)**
- Kotlin domain models mirroring all 7 page types: Note, Password, Recipe, Quote, HomeInventory, Reminder, ShoppingList
- `PageContent` sealed class with `@Serializable` subtypes; `ShoppingListItem.name` matches web canonical shape
- `ReminderTag`, `ReminderRecurrence`, `NotifyUnit` enums with lowercase `@SerialName` values
- `CategoryType`, `PageType` enums with display labels and icon mappings
- Complete DTO layer + `DomainMappers.kt` extension functions for all API response types

**Crypto engine (Phase 3)**
- `CryptoServiceImpl`: AES-256-GCM encrypt/decrypt via JCE; PBKDF2-SHA256 at 310,000 iterations
- Dual-format decrypt: mobile wire format `[tag‖ciphertext]` tried first, web format `[ciphertext‖tag]` as fallback — both clients can read each other's data
- `KeyCache`: `ConcurrentHashMap`-backed in-memory vault key store; key bytes zeroed on eviction
- `PasswordGenerator`: 88-char charset, rejection-sampling for uniform distribution, strength scorer matching TypeScript algorithm
- 29 unit tests including real cross-client known-answer vectors generated from Node.js crypto

**Network layer (Phase 4)**
- `TokenStore`: in-memory `StateFlow<AuthState>` for JWT access token + current user
- `PersistentCookieJar`: persists server's `httpOnly` refresh-token cookie in `EncryptedSharedPreferences` (AES-256-GCM, Android Keystore)
- `AuthInterceptor`: attaches `Bearer` token to every outgoing request
- `TokenRefreshAuthenticator`: silent 401 recovery via `@Named("auth")` OkHttp client; breaks DI cycle; clears credentials on unrecoverable failure
- 8 Retrofit API services: Auth, Categories, Pages, Attachments, Search, Reminders, ShoppingLists, Backup
- `NetworkModule`: two named OkHttp clients (`auth` plain / `main` with interceptors); base URL read from user-configurable DataStore
- `UserPreferencesDataStore`: DataStore-backed API URL, theme, inactivity timeout, font size preferences
- `ResponseExt`: `bodyOrThrow()` / `throwIfError()` helpers for clean `Result<T>` repository returns

**Room database + Repository layer (Phase 5)**
- Room database v1 with `CategoryEntity`, `PageEntity` (FK + indices), `AttachmentEntity` (FK cascade)
- DAOs with transactional `replaceAll` / `replaceByCategoryId` for offline-safe sync
- `PageDao.searchByTitle`: local LIKE search across cached page titles
- Repository interfaces in domain layer (`CategoryRepository`, `PageRepository`, `AttachmentRepository`)
- Offline-first implementations: Room as source of truth, API populates cache on `sync()` / `fetchPage()`
- `DatabaseModule` + `RepositoryModule` Hilt bindings

**Auth UI + Inactivity timeout (Phase 6)**
- `StartupViewModel`: resolves initial route (API URL setup → Login → Category list) from DataStore + TokenStore
- `ApiUrlScreen`: first-launch server URL setup with `/api/health` validation; reused for Settings
- `LoginScreen` / `RegisterScreen` / `ResetPasswordScreen`: Material 3, password visibility toggle, inline validation, Snackbar error feedback
- `VaultUnlockScreen` + `VaultUnlockViewModel`: derives AES-256 key from vault salt via PBKDF2, stores in `KeyCache`; shows password hint
- `InactivityManager`: coroutine timer driven by DataStore timeout setting; evicts all vault keys and logs out on expiry; reset on every pointer event via `pointerInteropFilter` in `MainActivity`
- Global logout observer in `AppNavGraph`: any `Unauthenticated` emission redirects to Login from any screen

**Core app — Categories, Pages, Editors (Phase 7)**
- `PageContentJson`: `parsePageContent(json, type)` / `serializePageContent(content)` / `defaultContent(type)` utilities using a shared lenient `Json` instance
- `CategoryListViewModel` + `CategoryListScreen`: `PullToRefreshBox` vault list; lock/unlock icon indicator per card; FAB → create sheet; long-press / MoreVert dropdown (Edit, Favorite, Lock, Delete)
- `CategoryCreateEditSheet`: `ModalBottomSheet`; type chips (General / Vault) hidden on edit; vault password + hint fields shown conditionally; generates PBKDF2 salt inline for new vaults
- `PageListViewModel` + `PageListScreen`: pulls `categoryId` from `SavedStateHandle`; observes pages + category from Room; pull-to-refresh; FAB → `PageTypePickerSheet`; per-page dropdown (Favorite, Delete)
- `PageTypePickerSheet`: `ModalBottomSheet` listing all 7 `PageType` entries with icon + label
- `PageDetailViewModel` + `PageDetailScreen`: create mode (from `pageType` arg) and edit mode (decrypt-on-load, encrypt-on-save); discard confirmation `BackHandler`; lock-state message; save checkmark
- 7 page type editors:
  - `NoteEditor`: transparent-border `OutlinedTextField`, `heightIn(min=300dp)`
  - `PasswordEditor`: URL, username (copy), password (visibility + copy + generate), strength `LinearProgressIndicator`, TOTP, notes
  - `QuoteEditor`: text, author, source; `ImeAction.Done` tag input with `FlowRow` chip list
  - `HomeInventoryEditor`: item name, description, location, value (decimal keyboard), purchase/warranty dates, serial number
  - `RecipeEditor`: servings + prep/cook time row; ingredient and step lists with add-via-keyboard-done + remove; notes
  - `ReminderEditor`: date / end date; `FilterChip` tag and recurrence selectors; `Switch` for notifications; notify-before quantity + unit chips
  - `ShoppingListEditor`: item + quantity add row; `Checkbox` toggle per item; remove button; notes
- `MainBottomBar`: `NavigationBar` with 4 destinations — Vaults, Search, Reminders, Shopping
- `AppNavGraph` fully wired: all placeholder screens replaced; `PAGE_CREATE` route includes `?pageType=` query param; `navArgument` declarations for all parameterised routes
- `Routes.pageCreate(categoryId, pageType)` updated to pass `pageType` as query param

### Fixed — Android
- `gradle.properties`: added `android.useAndroidX=true` (required for AndroidX dependency resolution) and `android.suppressUnsupportedCompileSdk=35`
- Added `com.google.android.material:material:1.12.0` dependency for `Theme.Material3.DayNight.NoActionBar` XML theme used by the `Activity` window
- `NetworkModule`: corrected `asConverterFactory` import from deprecated JakeWharton artifact path to official Retrofit 2.11 package (`retrofit2.converter.kotlinx.serialization`)
- `Icons.Filled.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack` in all 6 screens (`ApiUrlScreen`, `RegisterScreen`, `ResetPasswordScreen`, `PageListScreen`, `PageDetailScreen`, `VaultUnlockScreen`)

**Self-contained rewrite (Phase 8)**

### Changed — Android
- **No external network access**: Removed entire Retrofit/OkHttp stack, all 8 API services, token refresh flow, auth interceptors, and all auth screens (Login, Register, Reset Password, API URL setup)
- **Local-only data**: `CategoryRepositoryImpl` and `PageRepositoryImpl` now do pure Room CRUD with `UUID.randomUUID()` IDs and `Instant.now()` timestamps — no sync operations
- **Per-category vault passwords + biometric unlock**: replaced app-level auth with per-category password protection for encrypted (Vault-type) categories
- **Room schema v2**: dropped `PageEntity.encryptionSalt` (encryption is per-category; pages use the category's key from `KeyCache`) and dropped `AttachmentEntity` entirely
- **`StartupViewModel`**: simplified to always emit `Routes.CATEGORY_LIST` directly — no DataStore check, no auth gate
- **`InactivityManager`**: removed `TokenStore` and `LogoutUseCase` dependency; on timeout simply calls `keyCache.clearAll()`
- **`AppNavGraph`**: removed all auth routes (`LOGIN`, `REGISTER`, `RESET_PW`, `API_URL`) and the global logout observer; start destination is always `CATEGORY_LIST`
- **`Routes`**: removed auth route constants; removed `SETTINGS_API_URL`; removed `CATEGORY_CREATE` constant (create handled via sheet)
- **`CategoryListScreen` / `PageListScreen`**: replaced `PullToRefreshBox` with plain `Box` (no more sync/refresh)
- **`AndroidManifest.xml`**: removed `INTERNET` and `ACCESS_NETWORK_STATE` permissions and `networkSecurityConfig`; `USE_BIOMETRIC` was already present

### Added — Android (Phase 8)
- **`BiometricKeyStore`**: Android Keystore key management — creates AES-256-GCM keys requiring biometric auth; `encryptCipher` / `decryptCipher` for `BiometricPrompt.CryptoObject` usage
- **`VaultKeyStore`**: `EncryptedSharedPreferences`-backed storage for per-category biometric-encrypted vault key bytes + GCM IV
- **`VaultUnlockViewModel`** (biometric): `prepareBiometricEnroll()` / `onBiometricEnrollSuccess(cipher)` for optional key enrollment after password unlock; `prepareBiometricUnlock()` / `onBiometricUnlockSuccess(cipher)` for subsequent biometric logins
- **`VaultUnlockScreen`** (biometric): "Use biometrics" button when enrolled; "Enable biometric unlock" offer after password unlock; `BiometricPrompt` with `CryptoObject(cipher)` via `FragmentActivity` reference

### Removed — Android (Phase 8)
- All `data/remote/` classes: DTOs, API services, mappers, `TokenStore`, `PersistentCookieJar`, `AuthInterceptor`, `TokenRefreshAuthenticator`, `ResponseExt`
- `di/NetworkModule.kt`, `di/PreferencesModule.kt`
- Auth screens: `LoginScreen`, `RegisterScreen`, `ResetPasswordScreen`, `ApiUrlScreen`, `AuthViewModel`, `ApiUrlViewModel`
- Domain models: `User`, `BackupEntry`, `Attachment`, `SearchResult`, unused page subtypes
- `AttachmentRepository` + `AttachmentRepositoryImpl`
- Stale unit tests for deleted classes (`CategoryRepositoryTest`, `PageRepositoryTest`, `DomainMappersTest`, `TokenStoreTest`, `AuthInterceptorTest`, `TokenRefreshAuthenticatorTest`)
- Gradle dependencies: Retrofit, OkHttp, Play Billing

### Fixed — Android (Phase 8)
- `CryptoServiceTest`: fixed `tryDecryptMobileFormat` to use `result.iv` (fresh random IV) rather than the all-zero `KNOWN_IV_B64` constant
- `CryptoServiceTest`: replaced incorrect RFC 6070 / SHA-256 expected-value test with a simple byte-length assertion (RFC 6070 only defines SHA-1 vectors)
- Added `junit-platform-launcher` to `testRuntimeOnly` to satisfy Gradle 9.x JUnit Platform discovery requirement
- Deleted stale `ExampleUnitTest.java` (JUnit 4, incompatible with JUnit 5 project)

### Added — Android (post-Phase 8)

**Splash screen**
- `SplashScreen`: radial gradient dark background (`#1E1035` → `#0F0A1E`); vector vault logo (`ic_vault_logo.xml`) with purple ring, cardinal bolts, inner dial, indicator; tagline "What is your legacy?"; "Enter Legacy Vault" button
- `Icon(tint = Color.Unspecified)` used to preserve vector drawable's own colours without Material3 tinting
- `Routes.SPLASH` added as `startDestination`; pops inclusive on Enter so back stack is clean

**Cross-vault type views**
- `PageDao.observeByType(type)`: new `Flow<List<PageEntity>>` DAO query ordered by title/updated
- `PageRepository.observeByType(type)`: new interface + impl method
- `RemindersViewModel` + `RemindersScreen`: observes all `Reminder` pages across every vault via `observeByType`; `TypedPageCard` shared composable (internal) with type icon, lock indicator, favorite/delete dropdown
- `ShoppingViewModel` + `ShoppingScreen`: same pattern for `ShoppingList` pages; imports `TypedPageCard` from reminders package
- Both screens wired into `AppNavGraph` with full `onPageClick` + `onNavigate` callbacks

**Search**
- `SearchViewModel`: debounced (300 ms) query flow → `pageRepository.searchLocal()`; `isSearching` flag
- `SearchScreen`: auto-focused `TextField` in `TopAppBar`; clear button; empty/hint/results/no-results states; replaced `PlaceholderScreen("Search")` stub
- `PlaceholderScreen` removed from `AppNavGraph` entirely

**Backup & Restore**
- `BackupModels.kt`: `@Serializable` `VaultBackup`, `CategoryBackupDto`, `PageBackupDto` DTOs; `toBackupDto()` / `toEntity()` extension functions on Room entities
- `BackupRepository`: `exportJson()` reads all categories + pages → `kotlinx.serialization` pretty JSON; `importJson()` upserts all entities + recalculates page counts
- `CategoryDao.getAll()` + `CategoryDao.recalculatePageCount(id)` new DAO methods
- `PageDao.getAll()` new DAO method
- `BackupViewModel` (`AndroidViewModel`): `prepareExport()` / `writeExportToUri(Uri)` / `cancelExport()` / `importFromUri(Uri)` / `clearMessage()`; uses `ContentResolver` for file I/O; `pendingExportJson` state drives `CreateDocument` launcher
- `BackupScreen`: `CreateDocument("application/json")` + `OpenDocument()` launchers via `rememberLauncherForActivityResult`; spinner during work; export + restore buttons with descriptions; success/failure Snackbar
- Accessible via `⋮` overflow menu on the Vaults screen (Backup & Restore + Settings items added to `TopAppBar` actions)

**Settings**
- `SettingsViewModel`: `combine(theme, inactivityTimeoutMinutes, fontSize)` → `SettingsUiState` StateFlow backed by `UserPreferencesDataStore`
- `SettingsScreen`: `ExposedDropdownMenuBox` pickers for theme (System/Dark/Light), font size (Small/Medium/Large), and inactivity lock timeout (1/5/10/30 min/Never)
- `LegacyVaultTheme`: new `fontScale: Float` parameter applied via `CompositionLocalProvider(LocalDensity)` — scales all `sp` text app-wide
- `MainActivity`: injects `UserPreferencesDataStore`; collects `theme` + `fontSize` flows inside `setContent {}` as `collectAsStateWithLifecycle`; maps to `darkTheme` bool and `fontScale` float; passes both to `LegacyVaultTheme`
- **Bug fix**: theme and font size settings were persisted but never consumed — `LegacyVaultTheme()` was called with no arguments in `MainActivity`

**Recipe → Add ingredients to Shopping List**
- `PageDetailUiState`: new `showShoppingListPicker: Boolean` + `addedToListName: String?` fields
- `PageDetailViewModel`: new `shoppingLists: StateFlow<List<PageSummary>>` from `observeByType(ShoppingList)`; `showShoppingListPicker()` / `dismissShoppingListPicker()` / `addIngredientsToShoppingList(target, ingredients)` — loads target page, decrypts if needed (with `KeyCache` fallback error), appends `ShoppingListItem` entries, re-encrypts if needed, saves via `pageRepository.update()`
- `RecipeEditor`: new `onAddToShoppingList: (() -> Unit)?` parameter; "Add ingredients to shopping list" `OutlinedButton` (with `ShoppingCart` icon) shown below ingredient list when at least one ingredient exists
- `PageDetailScreen`: `shoppingLists` collected from ViewModel; shopping list picker `AlertDialog` shown when `showShoppingListPicker`; success Snackbar on `addedToListName`
- `PageContentEditor`: `onAddToShoppingList` threaded through to `RecipeEditor`

**Quote → Clipboard copy**
- `QuoteEditor`: `LocalClipboardManager` + copy `IconButton` overlaid on quote text field; formats as `"text" — Author (Source)` using Unicode curly quotes and em dash; button disabled and dimmed when text is empty

### Added — iOS (`LegacyVault.iOS`)
- New Flutter project at `src/LegacyVault.iOS/` — iOS port of the Android app, self-contained (no API dependency)
- **51 files** covering the full feature set
- **State management**: `flutter_riverpod` — `StateNotifier<S>` + `StateNotifierProvider` for every feature
- **Navigation**: `go_router` with `ShellRoute` for 4-tab bottom nav (Vaults, Search, Reminders, Shopping); full-screen routes for unlock, page detail, settings, backup
- **Database**: `sqflite` singleton (`DatabaseHelper`) with identical schema to Android Room — `categories` + `pages` tables, FK cascade, same column names
- **Crypto** (`CryptoServiceImpl`): `pointycastle` — `PBKDF2KeyDerivator(HMac(SHA256Digest(), 64))` at 310,000 iterations; `GCMBlockCipher(AESEngine)` with 12-byte IV; mobile wire format `Base64([tag|ciphertext])`; decrypt tries mobile format first, falls back to web format — interoperable with Android and web clients
- **Key management**: `KeyCache` in-memory singleton (never persisted); `VaultKeyStore` (`flutter_secure_storage`) for biometric-enrolled key bytes
- **Biometric unlock**: `local_auth` — enrollment offered after password unlock; subsequent logins use Face ID / Touch ID
- **All 7 page editors**: Note, Password (visibility toggle + copy + generate), Recipe (add-to-shopping-list), Quote (clipboard copy), HomeInventory, Reminder, ShoppingList
- **Backup**: export to JSON file / restore from JSON file via `file_picker`; format identical to Android — backups are cross-platform
- **Settings**: theme (dark/light/system) + font size applied at `MaterialApp` root; inactivity timeout via `shared_preferences`
- `ios/Runner/Info.plist`: `NSFaceIDUsageDescription` added for Face ID permission

---

## [0.1.0] — 2026-03-18

### Added
- Initial project scaffold: `LegacyVault.API`, `LegacyVault.Web`, `LegacyVault.Mobile`, `LegacyVault.Android`
- ASP.NET Core REST API with JWT authentication and refresh tokens
- SQLite database via Entity Framework Core with migrations
- Client-side AES-256-GCM encryption with PBKDF2 key derivation
- Per-category vault passwords with in-memory key cache
- Page types: Note, Password, Recipe, Quote, Home Inventory, Reminder, Shopping List
- General and Vault category types (Vault categories always encrypted)
- File/image attachment support
- Recurring reminders with in-app notifications
- Full-text global search
- Backup and restore functionality
- React + TypeScript SPA with Redux Toolkit state management
- Expo React Native mobile app with Expo Router v4
- Dark/Light theme with system-aware toggle
- Docker Compose self-hosted deployment (API + Web + SQLite volume)
- Nginx production config for the web container
- Branding: "Legacy Vault", purple accent (`#7c3aed`), inline SVG safe/vault logo
