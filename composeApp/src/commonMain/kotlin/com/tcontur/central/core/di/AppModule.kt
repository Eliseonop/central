package com.tcontur.central.core.di

import com.tcontur.central.data.repository.AuthRepositoryImpl
import com.tcontur.central.data.repository.EmpresaRepositoryImpl
import com.tcontur.central.domain.repository.AuthRepository
import com.tcontur.central.domain.repository.EmpresaRepository
import com.tcontur.central.domain.usecase.CheckLoginStatusUseCase
import com.tcontur.central.domain.usecase.GetEmpresasUseCase
import com.tcontur.central.domain.usecase.LoginUseCase
import com.tcontur.central.domain.usecase.LogoutUseCase
import com.tcontur.central.domain.usecase.SendLocationUseCase
import com.tcontur.central.presentation.auth.login.LoginViewModel
import com.tcontur.central.presentation.role.inspectoria.home.InspectoriaHomeViewModel
import com.tcontur.central.presentation.splash.SplashViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ─── Repositories ──────────────────────────────────────────────────────
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<EmpresaRepository> { EmpresaRepositoryImpl(get(), get()) }

    // ─── Use Cases ─────────────────────────────────────────────────────────
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { CheckLoginStatusUseCase(get()) }
    single { GetEmpresasUseCase(get()) }
    single { SendLocationUseCase(get()) }

    // ─── ViewModels ────────────────────────────────────────────────────────
    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { InspectoriaHomeViewModel(get(), get(), get()) }
}

/** All Koin modules shared across platforms. */
val commonModules = listOf(appModule, networkModule, storageModule)
