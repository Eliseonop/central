package com.tcontur.central.core.di

import android.content.Context
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.location.AndroidLocationManager
import com.tcontur.central.core.location.background.BackgroundServiceManager
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<LocationManager> { AndroidLocationManager(context) }
    single { BackgroundServiceManager(context) }
}
