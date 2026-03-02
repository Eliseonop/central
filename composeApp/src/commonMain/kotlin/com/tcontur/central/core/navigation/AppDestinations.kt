package com.tcontur.central.core.navigation

import kotlinx.serialization.Serializable

// ─── Top-level routes ───────────────────────────────────────────────────────

@Serializable
object Splash

@Serializable
object Login

// Role-based root destinations (each owns its own nested nav graph)
@Serializable
object InspectoriaRoot

@Serializable
object ConductorRoot   // Placeholder – extend when role is ready

@Serializable
object AdminRoot       // Placeholder – extend when role is ready

// ─── Inspectoria nested routes ───────────────────────────────────────────────

@Serializable
object InspectoriaHome

@Serializable
object LocationPermission

@Serializable
object LocationService
