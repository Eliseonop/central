package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    @SerialName("id")     val id: Int,
    @SerialName("nombre") val nombre: String
)
