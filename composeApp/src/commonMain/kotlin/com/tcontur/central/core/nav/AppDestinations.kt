package com.tcontur.central.core.nav

import kotlinx.serialization.Serializable


@Serializable
object Splash

@Serializable
object Login

@Serializable
object InspectoriaRoot

@Serializable
object ConductorRoot   // Placeholder – extend when role is ready

@Serializable
object AdminRoot       // Placeholder – extend when role is ready


@Serializable
data class SocketLoading(val role: String)


// ── Inspectoria destinations ───────────────────────────────────────────────────

/** Dashboard principal de inspectoría (reemplaza el WebView). */
@Serializable
object InspectoriaDashboard

/** Pantalla para iniciar una nueva inspección. */
@Serializable
object IniciarInspeccion

/** Pantalla de edición de una inspección activa. */
@Serializable
data class InspeccionActiva(val id: Int)

// ── Shared utility destinations ───────────────────────────────────────────────

@Serializable
object LocationPermission

@Serializable
object LocationService

// ── Legacy (no longer navigated to) ──────────────────────────────────────────
@Serializable
object InspectoriaHome
