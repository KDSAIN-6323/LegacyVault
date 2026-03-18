Update CHANGELOG.md with recent changes.

1. Run `git log --oneline --no-merges $(git describe --tags --abbrev=0 2>/dev/null || git rev-list --max-parents=0 HEAD)..HEAD 2>&1` to get commits since the last tag (or since the first commit if no tags exist).

2. Read the current `CHANGELOG.md`.

3. Identify what changed across `src/LegacyVault.API`, `src/LegacyVault.Web`, `src/LegacyVault.Mobile`, and `src/LegacyVault.Android`. Group changes into these categories (omit empty ones):
   - **Added** — new features
   - **Changed** — changes to existing functionality
   - **Fixed** — bug fixes
   - **Removed** — removed features
   - **Security** — security-related changes

4. Prepend a new `## [Unreleased] — YYYY-MM-DD` section to `CHANGELOG.md` (using today's date) with the grouped entries. If an `[Unreleased]` section already exists, append to it rather than creating a duplicate.

5. Keep entries concise — one line per change, present tense (e.g. "Add password strength indicator to PasswordEditor").

6. Show the user a preview of the new section before writing.
