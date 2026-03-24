# setup-env.ps1
# Generates or updates .env with OS-correct paths for the current machine.
# Works on Windows, macOS, and Linux (requires PowerShell Core / pwsh).
#
# Usage:
#   pwsh ./setup-env.ps1              # uses default vault dir under ~/Documents/.vault
#   pwsh ./setup-env.ps1 -VaultDir /custom/path

param(
    [string]$VaultDir = "",
    [int]$ApiPort = 0
)

$EnvFile = Join-Path $PSScriptRoot ".env"
$EnvExample = Join-Path $PSScriptRoot ".env.example"

# ── Resolve vault base dir ────────────────────────────────────────────────────
if (-not $VaultDir) {
    if ($IsWindows -or ($env:OS -eq "Windows_NT")) {
        $docs = [Environment]::GetFolderPath("MyDocuments")
        # Normalize to forward slashes for Docker volume mounts
        $VaultDir = ($docs + "\.vault") -replace '\\', '/'
    } else {
        $VaultDir = "$HOME/Documents/.vault"
    }
}

# Strip trailing slash
$VaultDir = $VaultDir.TrimEnd('/', '\')

$UploadsPath = "$VaultDir/uploads"
$BackupsPath = "$VaultDir/backups"

# ── Resolve API host port ─────────────────────────────────────────────────────
if ($ApiPort -eq 0) {
    # Default 5000; collaborators can override if that port is taken locally
    $ApiPort = 5000
}

# ── Read existing .env or seed from .env.example ──────────────────────────────
if (Test-Path $EnvFile) {
    $lines = Get-Content $EnvFile
    Write-Host "Updating existing .env ..."
} elseif (Test-Path $EnvExample) {
    $lines = Get-Content $EnvExample
    Write-Host "Creating .env from .env.example ..."
} else {
    $lines = @()
    Write-Host "Creating new .env ..."
}

# ── Update or append each key ─────────────────────────────────────────────────
function Set-EnvKey {
    param([string[]]$Lines, [string]$Key, [string]$Value)

    $pattern = "^\s*$Key\s*="
    $newLine  = "$Key=$Value"
    $found    = $false

    $result = $Lines | ForEach-Object {
        if ($_ -match $pattern) {
            $found = $true
            $newLine
        } else {
            $_
        }
    }

    if (-not $found) {
        $result += $newLine
    }

    return $result
}

$lines = Set-EnvKey -Lines $lines -Key "VAULT_UPLOADS_PATH" -Value $UploadsPath
$lines = Set-EnvKey -Lines $lines -Key "VAULT_BACKUPS_PATH" -Value $BackupsPath
$lines = Set-EnvKey -Lines $lines -Key "API_PORT"           -Value $ApiPort

# ── Write result ──────────────────────────────────────────────────────────────
$lines | Set-Content $EnvFile -Encoding UTF8

Write-Host ""
Write-Host "Done. .env written with:"
Write-Host "  VAULT_UPLOADS_PATH=$UploadsPath"
Write-Host "  VAULT_BACKUPS_PATH=$BackupsPath"
Write-Host "  API_PORT=$ApiPort"
