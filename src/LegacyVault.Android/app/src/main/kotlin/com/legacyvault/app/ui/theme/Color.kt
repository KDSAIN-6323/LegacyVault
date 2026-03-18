package com.legacyvault.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Brand palette ────────────────────────────────────────────────────────────
// Sourced from LegacyVault.Web/src/theme.css

// Dark theme surface colours
val DarkSurface       = Color(0xFF1E1E2E)   // sidebar-bg / content-bg
val DarkSurfaceVariant= Color(0xFF2A2A3E)   // pagelist-bg / card-bg
val DarkCard          = Color(0xFF24273A)
val DarkBorder        = Color(0xFF313244)

// Dark theme text
val DarkOnSurface     = Color(0xFFCDD6F4)   // primary text ≈ 9.8:1 on DarkSurface
val DarkOnSurfaceMuted= Color(0xFF9399B2)   // muted text  ≈ 5.4:1
val DarkHeading       = Color(0xFFE6E9F2)

// Dark theme accent (blue)
val DarkAccent        = Color(0xFF89B4FA)   // ≈ 5.5:1 on DarkSurface
val DarkAccentVariant = Color(0xFF74C7EC)
val DarkOnAccent      = Color(0xFF1E1E2E)

// Dark semantic
val DarkError         = Color(0xFFF38BA8)

// Light theme surface colours
val LightSurface      = Color(0xFFFFFFFF)
val LightSurfaceVariant=Color(0xFFF9FAFB)
val LightSidebarBg    = Color(0xFF1E2A45)
val LightSidebarVariant=Color(0xFF283452)

// Light theme text
val LightOnSurface    = Color(0xFF1F2937)   // ≈ 16.1:1 on white
val LightOnSurfaceMuted=Color(0xFF6B7280)   // ≈ 4.6:1
val LightHeading      = Color(0xFF111827)

// Light theme accent (darker blue for contrast)
val LightAccent       = Color(0xFF2563EB)   // ≈ 5.0:1 on white
val LightAccentVariant= Color(0xFF1D4ED8)
val LightOnAccent     = Color(0xFFFFFFFF)

// Light semantic
val LightError        = Color(0xFFDC2626)

// Neutral
val Black             = Color(0xFF000000)
val White             = Color(0xFFFFFFFF)
