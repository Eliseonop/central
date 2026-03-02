package com.tcontur.central.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary          = TconturBlue,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = TconturBlueLight,
    secondary        = TconturAccent,
    onSecondary      = OnSurface,
    background       = Surface,
    onBackground     = OnSurface,
    surface          = Surface,
    onSurface        = OnSurface,
    error            = Error,
    onError          = OnError
)

private val DarkColors = darkColorScheme(
    primary          = TconturBlueLight,
    onPrimary        = OnSurface,
    primaryContainer = TconturBlueDark,
    secondary        = TconturAccent,
    background       = OnSurface,
    surface          = OnSurface,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content
    )
}
