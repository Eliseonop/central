package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("id")       val id: Int,
    @SerialName("cargo")    val cargo: String,      // role / job title
    @SerialName("email")    val email: String,
    @SerialName("empresa")  val empresa: String,
    @SerialName("genero")   val genero: Boolean,
    @SerialName("nombre")   val nombre: String,
    @SerialName("token")    val token: String,
    @SerialName("username") val username: String
)
