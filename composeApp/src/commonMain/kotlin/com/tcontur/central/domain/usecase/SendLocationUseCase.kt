package com.tcontur.central.domain.usecase

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.utils.currentFormattedTimestamp
import com.tcontur.central.data.model.LocationRequest
import com.tcontur.central.data.remote.LocationApiService

class SendLocationUseCase(private val service: LocationApiService) {
    suspend operator fun invoke(
        empresaCodigo: String,
        token: String,
        lat: Double,
        lon: Double
    ): ApiResult<Unit> = service.sendLocation(
        empresaCodigo = empresaCodigo,
        token         = token,
        request       = LocationRequest(lat, lon, currentFormattedTimestamp())
    )
}
