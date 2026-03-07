package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("id")       val id: Int,
    @SerialName("cargo")    val cargo: String,      // descriptive job title (not used for role resolution)
    @SerialName("codigo")   val codigo: Int,         // empresa numeric code (comes as number from API)
    @SerialName("email")    val email: String,
    @SerialName("empresa")  val empresa: String,
    @SerialName("genero")   val genero: Boolean,
    @SerialName("nombre")   val nombre: String,
    @SerialName("token")    val token: String,
    @SerialName("username") val username: String,
    @SerialName("roles")    val roles: List<RoleDto> = emptyList()
)
