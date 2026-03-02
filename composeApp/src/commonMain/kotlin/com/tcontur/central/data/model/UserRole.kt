package com.tcontur.central.data.model

/**
 * Canonical roles recognised by the app.
 * The `cargo` field from the API is matched against these values.
 */
enum class UserRole(val key: String) {
    INSPECTORIA("inspector"),
    CONDUCTOR("conductor"),
    ADMIN("admin"),
    UNKNOWN("unknown");

    companion object {
        fun fromCargo(cargo: String): UserRole =
            entries.firstOrNull {
                cargo.lowercase().contains(it.key)
            } ?: UNKNOWN
    }
}
