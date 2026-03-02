package com.tcontur.central.domain.repository

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.domain.model.Empresa

interface EmpresaRepository {
    /** Fetches the list of companies from the remote API. */
    suspend fun getEmpresas(): ApiResult<List<Empresa>>

    /** Returns the locally-cached selected company, or null. */
    suspend fun getStoredEmpresa(): Empresa?

    /** Persists the selected company. */
    suspend fun saveEmpresa(empresa: Empresa)
}
