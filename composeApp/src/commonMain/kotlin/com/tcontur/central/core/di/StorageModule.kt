package com.tcontur.central.core.di

import com.russhwolf.settings.Settings
import com.tcontur.central.core.storage.AppStorage
import org.koin.dsl.module

val storageModule = module {
    single<Settings> { Settings() }
    single { AppStorage(get()) }
}
