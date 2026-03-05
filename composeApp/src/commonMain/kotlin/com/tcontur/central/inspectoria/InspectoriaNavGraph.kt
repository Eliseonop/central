package com.tcontur.central.inspectoria

import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.nav.InspectoriaHome
import com.tcontur.central.core.nav.InspectoriaRoot
import com.tcontur.central.core.nav.LocationPermission
import com.tcontur.central.core.nav.LocationService
import com.tcontur.central.inspectoria.home.InspectoriaHomeScreen
import com.tcontur.central.inspectoria.permission.LocationPermissionScreen
import com.tcontur.central.inspectoria.permission.LocationServiceScreen
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

fun NavGraphBuilder.inspectoriaNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<InspectoriaRoot>(startDestination = InspectoriaHome) {

        composable<InspectoriaHome> {
            val locationManager: LocationManager = koinInject()
            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

            // Re-check every time the screen becomes RESUMED (also on first open and
            // on every return from LocationPermission / LocationService).
            LaunchedEffect(lifecycleState) {
                if (lifecycleState == Lifecycle.State.RESUMED) {
                    when {
                        !locationManager.isPermissionGranted() ->
                            navController.navigate(LocationPermission) { launchSingleTop = true }
                        !locationManager.isServiceEnabled() ->
                            navController.navigate(LocationService) { launchSingleTop = true }
                        else -> Unit // all clear – show WebView
                    }
                }
            }

            InspectoriaHomeScreen(onLogout = onLogout)
        }

        composable<LocationPermission> {
            LocationPermissionScreen(
                onPermissionGranted = { navController.popBackStack() }
            )
        }

        composable<LocationService> {
            LocationServiceScreen(
                onServiceEnabled = { navController.popBackStack() }
            )
        }
    }
}
