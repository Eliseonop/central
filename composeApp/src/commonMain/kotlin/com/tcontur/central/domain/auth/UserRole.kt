package com.tcontur.central.domain.auth

import kotlinx.serialization.Serializable

/** Canonical app roles – derived from the API's `roles[]` list, not from `cargo`. */
@Serializable
enum class UserRole {
    INSPECTORIA,
    CONDUCTOR,
    ADMIN,
    UNKNOWN
}
