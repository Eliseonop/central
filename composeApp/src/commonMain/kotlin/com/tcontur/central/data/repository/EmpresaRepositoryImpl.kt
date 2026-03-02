package com.tcontur.central.data.repository

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.network.map
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.model.EmpresaResponse
import com.tcontur.central.data.remote.EmpresaApiService
import com.tcontur.central.domain.model.Empresa
import com.tcontur.central.domain.repository.EmpresaRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EmpresaRepositoryImpl(
    private val api: EmpresaApiService,
    private val storage: AppStorage
) : EmpresaRepository {

    override suspend fun getEmpresas(): ApiResult<List<Empresa>> =
        api.fetchEmpresas().map { list -> list.map { it.toDomain() } }

    override suspend fun getStoredEmpresa(): Empresa? {
        val json = storage.getString(StorageKeys.EMPRESA_JSON)
        return if (json.isBlank()) null
        else runCatching { Json.decodeFromString<EmpresaResponse>(json).toDomain() }.getOrNull()
    }

    override suspend fun saveEmpresa(empresa: Empresa) {
        val dto = EmpresaResponse(empresa.id, empresa.codigo, empresa.nombre)
        storage.putString(StorageKeys.EMPRESA_JSON, Json.encodeToString(dto))
    }

    private fun EmpresaResponse.toDomain() = Empresa(id, codigo, nombre)
}
