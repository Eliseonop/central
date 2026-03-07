package com.tcontur.central.domain.auth

import com.tcontur.central.data.model.UserResponse

/**
 * Resolves the app role from the API `roles[]` list.
 * Uses `nombre` contains-matching so variations like "Inspectoría", "Inspector",
 * "INSPECTORIA" all map correctly.
 */
fun UserResponse.resolveRole(): UserRole {
    val names = roles.map { it.nombre.lowercase() }
    return when {
        names.any { it.contains("inspector") } -> UserRole.INSPECTORIA
        names.any { it.contains("conductor") } -> UserRole.CONDUCTOR
        names.any { it.contains("admin")      } -> UserRole.ADMIN
        else                                    -> UserRole.UNKNOWN
    }
}
