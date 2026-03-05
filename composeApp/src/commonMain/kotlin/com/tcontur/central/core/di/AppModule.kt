package com.tcontur.central.core.di

import com.russhwolf.settings.Settings
import com.tcontur.central.core.network.HttpClientFactory
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.data.AuthApiService
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.EmpresaApiService
import com.tcontur.central.data.LocationApiService
import com.tcontur.central.inspectoria.home.InspectoriaHomeViewModel
import com.tcontur.central.inspectoria.loading.SocketLoadingViewModel
import com.tcontur.central.login.LoginViewModel
import com.tcontur.central.splash.SplashViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single<Settings> { Settings() }
    single { AppStorage(get()) }
    single { ProtoSocketManager() }
    single { AuthApiService(get()) }
    single { EmpresaApiService(get()) }
    single { LocationApiService(get()) }
    single { AuthRepositoryImpl(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { SocketLoadingViewModel(get(), get(), get(), get(), get()) }
    viewModel { InspectoriaHomeViewModel(get(), get(), get(), get()) }
}

val commonModules = listOf(appModule)
