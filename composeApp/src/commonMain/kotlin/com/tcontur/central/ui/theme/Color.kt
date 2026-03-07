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

// ─── Neutral ──────────────────────────────────────────────────────────────────
val Surface   = Color(0xFFE8EAED)   // gris humo claro
val OnSurface = Color(0xFF1A1A2E)   // texto oscuro (bien como está)
val Error     = Color(0xFFB00020)
val OnError   = Color(0xFFF2F2F2)   // blanco suavizado
