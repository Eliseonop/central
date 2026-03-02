package com.tcontur.central

import androidx.compose.ui.window.ComposeUIViewController
import com.tcontur.central.core.di.commonModules
import com.tcontur.central.core.di.iosModule
import org.koin.core.context.startKoin

/**
 * iOS entry point.
 * Called from iOSApp.swift / ContentView.swift via the Kotlin framework.
 */
fun MainViewController() = ComposeUIViewController {
    App()
}

/**
 * Must be called once from Swift before [MainViewController].
 * Initialises Koin with both shared and iOS-specific modules.
 */
fun initKoinIos() {
    startKoin {
        modules(commonModules + listOf(iosModule))
    }
}