package com.legacyvault.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legacyvault.app.R

// Brand purple — matches the vault logo and web palette (#7c3aed, #4c1d95)
private val BrandPurple      = Color(0xFF7C3AED)
private val BrandPurpleLight = Color(0xFFA78BFA)

@Composable
fun SplashScreen(onEnter: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1035), Color(0xFF0F0A1E)),
                    center = Offset(540f, 800f),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Vault logo ────────────────────────────────────────────────
            // Icon with Color.Unspecified preserves the vector's own colours.
            Icon(
                painter            = painterResource(R.drawable.ic_vault_logo),
                contentDescription = "Legacy Vault logo",
                tint               = Color.Unspecified,
                modifier           = Modifier.size(140.dp)
            )

            Spacer(Modifier.height(36.dp))

            // ── App name ──────────────────────────────────────────────────
            Text(
                text          = "Legacy Vault",
                fontSize      = 38.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                textAlign     = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(10.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Text(
                text      = "What is your legacy?",
                fontSize  = 17.sp,
                color     = BrandPurpleLight,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(72.dp))

            // ── Entry button ──────────────────────────────────────────────
            Button(
                onClick  = onEnter,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple,
                    contentColor   = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text          = "Enter Legacy Vault",
                    fontSize      = 16.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}
