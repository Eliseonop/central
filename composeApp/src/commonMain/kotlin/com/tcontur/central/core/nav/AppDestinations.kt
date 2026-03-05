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


@Serializable
object InspectoriaHome

@Serializable
object LocationPermission

@Serializable
object LocationService
