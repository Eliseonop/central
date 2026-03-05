package com.tcontur.central

import androidx.compose.runtime.Composable
import com.tcontur.central.core.nav.AppNavHost
import com.tcontur.central.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        AppNavHost()
    }
}