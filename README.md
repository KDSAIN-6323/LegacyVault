# Legacy Vault

A personal knowledge vault with client-side AES-256-GCM encryption. Store notes, passwords, recipes, reminders, shopping lists, home inventory, and quotes — all encrypted at rest, never readable by the server.

---

## Projects

| Project | Stack | Description |
|---------|-------|-------------|
| `src/LegacyVault.API` | ASP.NET Core / C# | REST API, SQLite, JWT auth, Dockerized |
| `src/LegacyVault.Web` | React + TypeScript (Vite) | SPA, Redux Toolkit, client-side crypto |
| `src/LegacyVault.Mobile` | Expo React Native | Cross-platform app, Expo Router v4 |
| `src/LegacyVault.Android` | Kotlin / Jetpack Compose | Native Android app — fully self-contained, no API required |
| `src/LegacyVault.iOS` | Flutter / Dart | Native iOS app — fully self-contained, no API required |

---

## Features

- **Encrypted Vaults** — AES-256-GCM encryption with PBKDF2 key derivation (310,000 iterations); vault password never leaves the device
- **Page Types** — Note, Password, Recipe, Quote, Home Inventory, Reminder, Shopping List
- **General & Vault categories** — General vaults store plain pages; Vault categories always encrypt content
- **Biometric Unlock** — Enroll Face ID / fingerprint after first password unlock on Android and iOS
- **Reminders** — Cross-vault view of all reminder pages
- **Shopping Lists** — Cross-vault view; add recipe ingredients directly to any shopping list
- **Global Search** — Full-text search across page titles
- **Backup & Restore** — Export/import encrypted JSON backup (cross-platform: Android ↔ iOS backups are interoperable)
- **Dark/Light/System theme** — Persisted per device
- **Font size** — Small / Medium / Large, applied app-wide
- **Inactivity lock** — Configurable timeout evicts all vault keys from memory
- **Self-hosted web** — Runs entirely via Docker Compose; no cloud dependency

---

## Quick Start (Web + API)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### Setup

1. Clone the repo and copy the env template:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` with your values:
   ```env
   JWT_SECRET=<64-character random string>
   VAULT_UPLOADS_PATH=/path/to/uploads
   VAULT_BACKUPS_PATH=/path/to/backups
   ```

3. Start the stack:
   ```bash
   docker-compose up
   ```

4. Open [http://localhost:8080](http://localhost:8080) and register your account.

---

## Development

### API (ASP.NET Core)
```bash
cd src/LegacyVault.API
dotnet run
```

### Web (Vite)
```bash
cd src/LegacyVault.Web
npm install
npm run dev
```

### Mobile (Expo)
```bash
cd src/LegacyVault.Mobile
npm install
npx expo start
```

### Android (Kotlin / Jetpack Compose)
```bash
cd src/LegacyVault.Android
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # install on connected device/emulator
./gradlew testDebugUnitTest      # run unit tests
```
Requires Android Studio (JDK provided at `Android Studio/jbr`). Min SDK 26, Target SDK 35.

### iOS (Flutter)
```bash
cd src/LegacyVault.iOS
flutter pub get
flutter build ios                # requires Xcode on macOS
flutter run                      # run on simulator or device
```
Requires Flutter SDK + Xcode. `NSFaceIDUsageDescription` is pre-configured in `ios/Runner/Info.plist`.

---

## Architecture

### Web / Expo Mobile
```
Browser / Expo App
      │  HTTPS + JWT Bearer
      ▼
  LegacyVault.API (ASP.NET Core)
      │
      ▼
  SQLite (Docker volume)
```

### Android & iOS (self-contained)
```
LegacyVault.Android / LegacyVault.iOS
      │
      ▼
  Local SQLite (on-device)
      │
  AES-256-GCM (in-memory keys, PBKDF2 derivation)
```

Encryption keys are derived from the vault password via PBKDF2 and held in memory only. No server is required — all data lives on the device. Backup files are encrypted at the page level; the JSON container is plaintext but vault content remains ciphertext.

---

## Crypto Contract

All clients (Web, Expo, Android, iOS) share the same wire format so backups and encrypted pages are interoperable:

| Parameter | Value |
|-----------|-------|
| Algorithm | AES-256-GCM |
| Key derivation | PBKDF2-SHA256 |
| Iterations | 310,000 |
| Salt length | 32 bytes (standard Base64) |
| Key length | 32 bytes |
| IV length | 12 bytes |
| Auth tag | 16 bytes, **prepended** to ciphertext |
| Wire format | `Base64([16-byte tag][ciphertext])` |

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWT signing key — minimum 64 characters, random |
| `VAULT_UPLOADS_PATH` | Host path for uploaded attachments |
| `VAULT_BACKUPS_PATH` | Host path for database backups |

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
