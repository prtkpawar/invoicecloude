package com.example.ui.theme.pro

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Danger600
import com.example.ui.theme.Ink100
import com.example.ui.theme.Ink200
import com.example.ui.theme.Ink400
import com.example.ui.theme.Ink50
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink800
import com.example.ui.theme.Ink950
import com.example.ui.theme.MonoDark
import com.example.ui.theme.MonoLight
import com.example.ui.theme.Success50
import com.example.ui.theme.Success600
import com.example.ui.theme.Warning50
import com.example.ui.theme.Warning600
import com.example.ui.theme.White

// ============================================================
// AURORA — Premium Black/White design system
// Uses centralized Obsidian color tokens from Color.kt
// ============================================================

val Ink900      = com.example.ui.theme.Ink950
val Ink800      = com.example.ui.theme.Ink800
val Ink600      = com.example.ui.theme.Ink600
val Ink400      = com.example.ui.theme.Ink400
val Ink200      = com.example.ui.theme.Ink200
val Ink100      = com.example.ui.theme.Ink100
val Ink50       = com.example.ui.theme.Ink50
val Line        = com.example.ui.theme.Ink200
val Canvas      = com.example.ui.theme.Ink50
val Surface     = com.example.ui.theme.White
val SurfaceCard = com.example.ui.theme.White
val SurfaceSoft = com.example.ui.theme.Ink100

val Brand700    = com.example.ui.theme.Ink800
val Brand600    = com.example.ui.theme.Ink950
val Brand100    = com.example.ui.theme.Ink100
val Brand50     = com.example.ui.theme.Ink50

val Success600  = com.example.ui.theme.Success600
val Success50   = com.example.ui.theme.Success50
val Danger600   = com.example.ui.theme.Danger600
val Danger50    = Color(0xFFFCEBEB)
val Warning600  = com.example.ui.theme.Warning600
val Warning50   = com.example.ui.theme.Warning50

val LightColors = lightColorScheme(
    primary          = Brand600,
    onPrimary        = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand700,
    secondary        = Ink600,
    background       = Canvas,
    onBackground     = Ink900,
    surface          = SurfaceCard,
    onSurface        = Ink900,
    surfaceVariant   = SurfaceSoft,
    onSurfaceVariant = Ink600,
    outline          = Line,
    error            = Danger600,
)

// ---- Typography: ONE scale ----
val AppType = Typography(
    displayLarge = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge   = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    titleMedium  = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge    = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium   = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelLarge   = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    labelMedium  = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall   = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

// ---- Shapes ----
val AppShapes = Shapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large  = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun AuroraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MonoDark else MonoLight,
        typography = AppType,
        shapes = AppShapes,
        content = content
    )
}

// ---- Spacing rhythm ----
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
