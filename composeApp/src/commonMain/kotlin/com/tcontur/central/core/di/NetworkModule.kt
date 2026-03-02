package com.tcontur.central.core.di

import com.tcontur.central.core.network.HttpClientFactory
import com.tcontur.central.data.remote.AuthApiService
import com.tcontur.central.data.remote.EmpresaApiService
import com.tcontur.central.data.remote.LocationApiService
import org.koin.dsl.module

val networkModule = module {
    single { HttpClientFactory.create() }
    single { AuthApiService(get()) }
    single { EmpresaApiService(get()) }
    single { LocationApiService(get()) }
}
