package com.tcontur.central.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Tema claro minimalista — fondo gris, cards/botones más saturados ────────
private val LightMinimalistColors = lightColorScheme(
    primary              = TconturBlue,           // #0066A9
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFBBDEFB),     // azul suave para containers
    onPrimaryContainer   = Color(0xFF003A70),
    secondary            = TconturAccent,
    onSecondary          = Color.White,
    background           = Surface,               // #EEF2F7 — gris azulado suave
    onBackground         = OnSurface,             // #1A2332
    surface              = SurfaceCard,           // blanco para sheets
    onSurface            = OnSurface,
    surfaceVariant       = Color(0xFFDDE6F0),     // gris-azul para variantes
    onSurfaceVariant     = OnSurfaceSub,          // #5A6A80
    outline              = Outline,               // #CBD5E1
    error                = Error,
    onError              = OnError,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightMinimalistColors,
        typography  = AppTypography,
        content     = content
    )
}
