package com.tcontur.central.inspectoria

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.nav.*
import com.tcontur.central.inspectoria.dashboard.InspectoriaDashboardScreen
import com.tcontur.central.inspectoria.iniciar.IniciarInspeccionScreen
import com.tcontur.central.inspectoria.inspeccion.InspeccionScreen
import com.tcontur.central.inspectoria.permission.LocationPermissionScreen
import com.tcontur.central.inspectoria.permission.LocationServiceScreen
import org.koin.compose.koinInject

fun NavGraphBuilder.inspectoriaNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<InspectoriaRoot>(startDestination = InspectoriaDashboard) {

        // ── Dashboard (start) ──────────────────────────────────────────────────
        composable<InspectoriaDashboard> {
            val locationManager: LocationManager = koinInject()
            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

            LaunchedEffect(lifecycleState) {
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    when {
                        !locationManager.isPermissionGranted() ->
                            navController.navigate(LocationPermission) { launchSingleTop = true }
                        !locationManager.isServiceEnabled() ->
                            navController.navigate(LocationService) { launchSingleTop = true }
                        else -> Unit
                    }
                }
            }

            InspectoriaDashboardScreen(
                onIniciar   = { navController.navigate(IniciarInspeccion) },
                onContinuar = { id -> navController.navigate(InspeccionActiva(id)) },
                onLogout    = onLogout
            )
        }

        // ── Iniciar inspección ─────────────────────────────────────────────────
        composable<IniciarInspeccion> {
            IniciarInspeccionScreen(
                onCreated = { id ->
                    navController.navigate(InspeccionActiva(id)) {
                        popUpTo(InspectoriaDashboard) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Inspección activa ──────────────────────────────────────────────────
        composable<InspeccionActiva> { backStackEntry ->
            val route: InspeccionActiva = backStackEntry.toRoute()
            InspeccionScreen(
                inspId     = route.id,
                onFinished = { navController.popBackStack(InspectoriaDashboard, inclusive = false) },
                onBack     = { navController.popBackStack() }
            )
        }

        // ── Permisos ───────────────────────────────────────────────────────────
        composable<LocationPermission> {
            LocationPermissionScreen(onPermissionGranted = { navController.popBackStack() })
        }

        composable<LocationService> {
            LocationServiceScreen(onServiceEnabled = { navController.popBackStack() })
        }
    }
}
