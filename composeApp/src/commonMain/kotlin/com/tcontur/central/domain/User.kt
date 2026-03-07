package com.tcontur.central.domain

import com.tcontur.central.domain.auth.UserRole

/** Clean domain entity – decoupled from the API serialization model. */
data class User(
    val id: Int,
    val nombre: String,
    val username: String,
    val email: String,
    val token: String,
    val cargo: String,
    val empresa: String,
    val codigo: Int,        // empresa numeric code – schema type is Number, sent as-is over socket
    val role: UserRole
)
