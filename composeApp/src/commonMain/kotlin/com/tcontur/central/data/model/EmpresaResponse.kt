package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmpresaResponse(
    @SerialName("id")     val id: Int,
    @SerialName("codigo") val codigo: String,   // subdomain prefix used in API URLs
    @SerialName("nombre") val nombre: String
) {
    /** Strips spaces from nombre for URL construction. */
    fun getNameNoSpaces(): String = nombre.replace(" ", "")
}
