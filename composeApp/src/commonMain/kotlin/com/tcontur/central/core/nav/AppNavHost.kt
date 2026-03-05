package com.tcontur.central.core.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tcontur.central.login.LoginScreen
import com.tcontur.central.inspectoria.loading.SocketLoadingScreen
import com.tcontur.central.inspectoria.inspectoriaNavGraph
import com.tcontur.central.splash.SplashScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash – determines where to navigate after checking auth state
        composable<Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateToRole = { role ->
                    // Always go through SocketLoading so the WS connects before home
                    navController.navigate(SocketLoading(role)) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable<Login> {
            LoginScreen(
                onLoginSuccess = { role ->
                    // Show full-screen loading while the WebSocket connects
                    navController.navigate(SocketLoading(role)) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        // Full-screen black loading screen – waits for WebSocket connection
        composable<SocketLoading> { backStackEntry ->
            val args = backStackEntry.toRoute<SocketLoading>()
            SocketLoadingScreen(
                onConnected = {
                    val destination = roleDestination(args.role)
                    navController.navigate(destination) {
                        popUpTo(SocketLoading(args.role)) { inclusive = true }
                    }
                }
            )
        }

        // Inspectoria role graph
        inspectoriaNavGraph(
            navController = navController,
            onLogout = {
                navController.navigate(Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        // Additional role graphs added here when implemented
        composable<ConductorRoot> { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
        composable<AdminRoot> { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
    }
}

/** Maps a user role string to the corresponding navigation destination. */
private fun roleDestination(role: String): Any = when (role.lowercase()) {
    "inspector", "inspectoria" -> InspectoriaRoot
    "conductor" -> ConductorRoot
    "admin", "administrador" -> AdminRoot
    else -> InspectoriaRoot // safe default
}
