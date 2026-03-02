package com.tcontur.central.core.di

import com.tcontur.central.core.location.IosLocationManager
import com.tcontur.central.core.location.LocationManager
import org.koin.dsl.module

val iosModule = module {
    single<LocationManager> { IosLocationManager() }
}
