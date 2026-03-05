package com.tcontur.central.core.di

import android.content.Context
import com.tcontur.central.core.location.AndroidLocationManager
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.location.background.BackgroundServiceManager
import com.tcontur.central.core.socket.AndroidSocketServiceManager
import com.tcontur.central.core.socket.SocketServiceManager
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<LocationManager> { AndroidLocationManager(context) }
    single { BackgroundServiceManager(context) }
    single<SocketServiceManager> { AndroidSocketServiceManager(context) }
}
