package com.tcontur.central

import android.app.Application
import com.tcontur.central.core.di.androidModule
import com.tcontur.central.core.di.commonModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application entry point for the Android launcher module.
 *
 * Bootstraps Koin with:
 *   - [commonModules]  → shared KMP modules (network, storage, use-cases, view-models)
 *   - [androidModule]  → Android-specific bindings (LocationManager, BackgroundServiceManager)
 */
class TconturApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TconturApplication)
            modules(commonModules + listOf(androidModule(this@TconturApplication)))
        }
    }
}
