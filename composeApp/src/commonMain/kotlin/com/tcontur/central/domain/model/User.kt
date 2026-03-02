package com.tcontur.central.domain.model

import com.tcontur.central.data.model.UserRole

/** Clean domain entity – decoupled from the API serialization model. */
data class User(
    val id: Int,
    val nombre: String,
    val username: String,
    val email: String,
    val token: String,
    val cargo: String,
    val empresa: String,
    val role: UserRole
)
