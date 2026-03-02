package com.tcontur.central.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tcontur.central.presentation.auth.login.LoginScreen
import com.tcontur.central.presentation.role.RoleRouter
import com.tcontur.central.presentation.role.inspectoria.navigation.inspectoriaNavGraph
import com.tcontur.central.presentation.splash.SplashScreen

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
                    val destination = roleDestination(role)
                    navController.navigate(destination) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable<Login> {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = roleDestination(role)
                    navController.navigate(destination) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        // Inspectoria role graph
        inspectoriaNavGraph(
            onLogout = {
                navController.navigate(Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        // Additional role graphs added here when implemented
        composable<ConductorRoot> { RoleRouter() }
        composable<AdminRoot> { RoleRouter() }
    }
}

/** Maps a user role string to the corresponding navigation destination. */
private fun roleDestination(role: String): Any = when (role.lowercase()) {
    "inspector", "inspectoria" -> InspectoriaRoot
    "conductor" -> ConductorRoot
    "admin", "administrador" -> AdminRoot
    else -> InspectoriaRoot // safe default
}
