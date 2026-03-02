package com.tcontur.central.presentation.role.inspectoria.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.tcontur.central.core.navigation.InspectoriaHome
import com.tcontur.central.core.navigation.InspectoriaRoot
import com.tcontur.central.core.navigation.LocationPermission
import com.tcontur.central.core.navigation.LocationService
import com.tcontur.central.presentation.role.inspectoria.home.InspectoriaHomeScreen
import com.tcontur.central.presentation.role.inspectoria.permission.LocationPermissionScreen
import com.tcontur.central.presentation.role.inspectoria.permission.LocationServiceScreen

/**
 * Nested navigation graph for the Inspectoria role.
 * Entry point is [InspectoriaRoot]; default screen is [InspectoriaHome].
 */
fun NavGraphBuilder.inspectoriaNavGraph(
    onLogout: () -> Unit
) {
    navigation<InspectoriaRoot>(startDestination = InspectoriaHome) {

        composable<InspectoriaHome> { backStack ->
            val parentEntry = backStack
            InspectoriaHomeScreen(
                onLogout = onLogout,
                onRequestLocationPermission = { navController ->
                    navController.navigate(LocationPermission)
                },
                onRequestLocationService = { navController ->
                    navController.navigate(LocationService)
                }
            )
        }

        composable<LocationPermission> {
            LocationPermissionScreen(
                onPermissionGranted = { navController ->
                    navController.popBackStack()
                }
            )
        }

        composable<LocationService> {
            LocationServiceScreen(
                onServiceEnabled = { navController ->
                    navController.popBackStack()
                }
            )
        }
    }
}
