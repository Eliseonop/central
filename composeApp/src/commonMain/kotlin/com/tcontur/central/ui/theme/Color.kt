package com.tcontur.central.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand palette (exact hex values from Flutter app) ────────────────────────
/** Main brand blue: #0066a9 — used at full opacity for login gradient bottom, icons */
val TconturBlue      = Color(0xFF0066A9)
/** Darker blue: #125183 — used for Home AppBar background */
val TconturAppBar    = Color(0xFF125183)
/** Orange accent — used for permission screen buttons */
val OrangeAccent     = Color(0xFFFFAB40)
/** Blue accent — used for permission screen headings */
val BlueAccent       = Color(0xFF448AFF)

// ─── Login gradient stops (same hue #0066a9, varying alpha top→bottom) ────────
val TconturGrad0     = Color(0x660066A9)   // 40 % opacity
val TconturGrad1     = Color(0x990066A9)   // 60 % opacity
val TconturGrad2     = Color(0xCC0066A9)   // 80 % opacity
val TconturGrad3     = Color(0xFF0066A9)   // 100 % opacity

// ─── Field icon color (#0066a9 at 60 % opacity) ───────────────────────────────
val TconturIconBlue  = Color(0x990066A9)

// ─── Legacy aliases (used by AppTheme + SplashScreen) ────────────────────────
val TconturBlueDark  = Color(0xFF003C8F)   // deep navy — splash gradient top, dark scheme
val TconturBlueLight = Color(0xFF5E92F3)   // light blue — light scheme primaryContainer
val TconturAccent    = Color(0xFFFFAB00)   // amber — MaterialTheme secondary

val TconturAccentDark    = Color(0xFFFF9800)   // amber — dark scheme secondary

// ─── Shared button palette (dark pastels — referencia: dashboard) ─────────────
/** Azul pastel oscuro — Scan / QR (fondo) */
val BtnBlueBg      = Color(0xFFB3D4F0)
/** Azul pastel oscuro — Scan / QR (texto e ícono) */
val BtnBlueFg      = Color(0xFF0D4C8A)
/** Verde pastel oscuro — Mapa / Finalizar (fondo) */
val BtnGreenBg     = Color(0xFF9DD4B0)
/** Verde pastel oscuro — Mapa / Finalizar (texto e ícono) */
val BtnGreenFg     = Color(0xFF145C32)
/** Gris neutro — botón inactivo (fondo) */
val BtnDisabledBg  = Color(0xFFDDE3EA)
/** Gris neutro — botón inactivo (texto e ícono) */
val BtnDisabledFg  = Color(0xFF7A8A99)

// ─── Neutral ──────────────────────────────────────────────────────────────────
val Surface        = Color(0xFFEEF2F7)   // fondo gris-azulado suave (app background)
val SurfaceCard    = Color(0xFFFFFFFF)   // blanco para sheets / drawers
val OnSurface      = Color(0xFF1A2332)   // texto principal oscuro
val OnSurfaceSub   = Color(0xFF5A6A80)   // texto secundario / placeholder
val Outline        = Color(0xFFCBD5E1)   // borde sutil
val Error          = Color(0xFFB00020)
val OnError        = Color(0xFFF2F2F2)
