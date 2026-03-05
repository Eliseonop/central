package com.tcontur.central.core.di

import com.tcontur.central.core.location.IosLocationManager
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.socket.IosSocketServiceManager
import com.tcontur.central.core.socket.SocketServiceManager
import org.koin.dsl.module

val iosModule = module {
    single<LocationManager> { IosLocationManager() }
    single<SocketServiceManager> { IosSocketServiceManager() }
}
