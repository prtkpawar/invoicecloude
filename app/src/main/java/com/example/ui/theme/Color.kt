package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// OBSIDIAN DESIGN SYSTEM — Monochrome Base + Indigo Accent (#4338CA)
// ============================================================

// Primary Ink Palette (Monochrome)
val Ink950 = Color(0xFF0A0A0B)   // Top bars, primary text
val Ink800 = Color(0xFF27272A)   // Secondary text, icons
val Ink600 = Color(0xFF52525B)   // Tertiary text, placeholders
val Ink400 = Color(0xFFA1A1AA)   // Disabled text, borders
val Ink200 = Color(0xFFE4E4E7)   // Dividers, card borders
val Ink100 = Color(0xFFF4F4F5)   // Card backgrounds, input fills
val Ink50  = Color(0xFFFAFAFA)   // Page background (canvas)
val White  = Color(0xFFFFFFFF)   // Surface (cards, sheets, dialogs)

// Accent Palette (Indigo — Action color)
val Accent700 = Color(0xFF4338CA) // Primary buttons, links, active indicators
val Accent100 = Color(0xFFE0E7FF) // Accent background, selected chips
val Accent50  = Color(0xFFEEF2FF) // Hover/pressed states

// Semantic Status Tokens
val Success600 = Color(0xFF16A34A) // Paid, received, positive
val Success50  = Color(0xFFF0FDF4)
val Warning600 = Color(0xFFD97706) // Partial, pending, due soon
val Warning50  = Color(0xFFFFFBEB)
val Danger600  = Color(0xFFDC2626) // Overdue, unpaid, delete
val Danger50   = Color(0xFFFEF2F2)

val Ink900     = Ink950
val Brand100   = Ink100
val Brand600   = Ink950
val Brand700   = Ink800

// ============================================================
// LEGACY COMPATIBILITY ALIASES
// ============================================================

val BizPrimary        = Ink950
val BizPrimaryDark    = Color(0xFF000000)
val BizPrimaryLight   = Ink100
val BizPrimarySubtle  = Ink200
val BizPrimaryBorder  = Ink400

val BizNavy           = Ink950
val BizNavySubtle     = Ink800
val BizSlate          = Ink800
val BizGray           = Ink600
val BizLightGray      = Ink100
val BizBorder         = Ink200

val BizGreen          = Success600
val BizGreenLight     = Success50
val BizGreenDark      = Success600

val BizRed            = Danger600
val BizRedLight       = Danger50
val BizRedDark        = Danger600

val BizAmber          = Warning600
val BizAmberLight     = Warning50

val BizPurple         = Accent700
val BizPurpleLight    = Accent100
val BizTeal           = Accent700
val BizTealLight      = Accent100

val BackgroundLight   = Ink50
val SurfaceLight      = White
val SurfaceCard       = White
val Canvas            = Ink50
val Line              = Ink200
val SurfaceSoft       = Ink100
val BorderLight       = Ink200

// Swiggy & Swami Legacy Aliases
val SwiggyOrange      = Accent700
val SwiggyOrangeDeep  = Accent700
val SwiggyOrangeLight = Accent100
val SwiggyOrangeSubtle= Accent50
val SwiggyOrangeBorder= Accent700

val SwiggyDark        = Ink950
val SwiggyCharcoal    = Ink800
val SwiggyGray        = Ink600
val SwiggyLightGray   = Ink100
val SwiggyBorder      = Ink200

val SwiggyGreen       = Success600
val SwiggyGreenLight  = Success50
val SwiggyGreenDark   = Success600

val SwamiNavy         = Ink950
val SwamiNavyDark     = Ink950
val SwamiNavyLight    = Ink800
val SwamiGold         = Warning600
val SwamiGoldLight    = Warning50
val SwamiGoldDark     = Warning600

val SwamiGreen        = Success600
val SwamiGreenLight   = Success50
val SwamiGreenDark    = Success600
val SwamiRed          = Danger600
val SwamiRedLight     = Danger50
val SwamiOrange       = Warning600
val SwamiOrangeLight  = Warning50
val SwamiTeal         = Accent700
val SwamiTealLight    = Accent100
val SwamiPurple       = Accent700
val SwamiPurpleLight  = Accent100

val NeutralDark       = Ink950
val NeutralMedium     = Ink600
val NeutralLight      = Ink400

// Gradients
val BizPrimaryGradient   = Brush.horizontalGradient(listOf(Ink800, Ink950))
val BizNavyGradient      = Brush.linearGradient(listOf(Ink800, Ink950))
val SwiggyPrimaryGradient= BizPrimaryGradient
val SolarHeroGradient    = BizNavyGradient
val GoldSubsidyGradient  = Brush.linearGradient(listOf(Ink800, Ink800))
val GreenRevenueGradient = Brush.linearGradient(listOf(Success600, Success600))
val DarkCivilGradient    = Brush.linearGradient(listOf(Ink800, Ink800))

