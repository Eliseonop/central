package com.tcontur.central.core.di

import com.russhwolf.settings.Settings
import com.tcontur.central.core.QrDataHolder
import com.tcontur.central.core.location.LocationRepository
import com.tcontur.central.core.network.HttpClientFactory
import com.tcontur.central.core.session.SessionManager
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.socket.SocketDispatcherViewModel
import com.tcontur.central.core.socket.SocketSessionRepository
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.data.AuthApiService
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.EmpresaApiService
import com.tcontur.central.data.InspeccionApiService
import com.tcontur.central.data.LocationApiService
import com.tcontur.central.data.repository.RoutesDataRepository
import com.tcontur.central.inspectoria.dashboard.InspectoriaDashboardViewModel
import com.tcontur.central.inspectoria.iniciar.IniciarInspeccionViewModel
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel
import com.tcontur.central.inspectoria.initializer.InspectoriaInitializerViewModel
import com.tcontur.central.login.LoginViewModel
import com.tcontur.central.splash.SplashViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create() }
    single<Settings> { Settings() }
    single { AppStorage(get()) }
    single { ProtoSocketManager() }
    single { RoutesDataRepository(get()) }
    // ── Socket session layer ───────────────────────────────────────────────────
    single { SocketSessionRepository() }
    // ── Auth / Session ─────────────────────────────────────────────────────────
    single { AuthApiService(get()) }
    single { EmpresaApiService(get()) }
    single { LocationApiService(get()) }
    single { InspeccionApiService(get()) }
    single { AuthRepositoryImpl(get(), get()) }
    single { SessionManager(get(), get(), get()) }     // + SocketSessionRepository
    single { QrDataHolder() }
    single { LocationRepository() }
    // ── ViewModels ─────────────────────────────────────────────────────────────
    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    // Central socket message dispatcher — single consumer of ProtoSocketManager.socketEvents.
    // Must be activated at app scope in AppNavHost so it lives for the full session.
    viewModel { SocketDispatcherViewModel(get(), get(), get(), get()) }
    viewModel { InspectoriaInitializerViewModel(get(), get(), get(), get(), get()) }
    viewModel { InspectoriaDashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { IniciarInspeccionViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { InspeccionViewModel(get(), get(), get(), get(), get()) }
}

val commonModules = listOf(appModule)
