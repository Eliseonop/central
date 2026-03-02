package com.tcontur.central.domain.usecase

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.domain.model.Empresa
import com.tcontur.central.domain.repository.EmpresaRepository

class GetEmpresasUseCase(private val repository: EmpresaRepository) {
    suspend operator fun invoke(): ApiResult<List<Empresa>> = repository.getEmpresas()
}
