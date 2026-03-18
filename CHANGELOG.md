# Changelog

All notable changes to Legacy Vault will be documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — 2026-03-18

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
