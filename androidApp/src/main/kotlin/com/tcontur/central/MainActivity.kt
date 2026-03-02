package com.tcontur.central

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat

/**
 * Single Android Activity.
 *
 * The entire Compose UI tree ([App]) is defined in the :composeApp KMP library.
 * This class is intentionally kept minimal — all logic lives in the shared module.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            App()   // App() composable comes from :composeApp (commonMain)
        }
    }
}
