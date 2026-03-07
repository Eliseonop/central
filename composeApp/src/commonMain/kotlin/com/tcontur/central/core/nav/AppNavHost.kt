package com.tcontur.central.core.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tcontur.central.core.network.SessionEventBus
import com.tcontur.central.core.permission.StartupPermissionsScreen
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.login.LoginScreen
import com.tcontur.central.inspectoria.loading.SocketLoadingScreen
import com.tcontur.central.inspectoria.inspectoriaNavGraph
import com.tcontur.central.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = StartupPermissions  // ← permissions gate is always first
) {
    val authRepository: AuthRepositoryImpl = koinInject()

    LaunchedEffect(Unit) {
        SessionEventBus.unauthorized.collect {
            authRepository.logout()
            navController.navigate(Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {

        // ── Permissions gate ──────────────────────────────────────────────────
        // Requests POST_NOTIFICATIONS + LOCATION before anything else.
        // If already granted (e.g. second launch), navigates to Splash in < 1 frame.
        composable<StartupPermissions> {
            StartupPermissionsScreen(
                onAllGranted = {
                    navController.navigate(Splash) {
                        popUpTo(StartupPermissions) { inclusive = true }
                    }
                }
            )
        }

        // ── Splash ────────────────────────────────────────────────────────────
        // Determines where to navigate after checking auth state.
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

        // ── Login ─────────────────────────────────────────────────────────────
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

        // ── Socket Loading ────────────────────────────────────────────────────
        // Full-screen loading – waits for WebSocket connection + login confirmation.
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

        // ── Inspectoria role graph ─────────────────────────────────────────────
        inspectoriaNavGraph(
            navController = navController,
            onLogout = {
                navController.navigate(Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        // ── Other role graphs (stubs) ──────────────────────────────────────────
        composable<ConductorRoot> { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
        composable<AdminRoot>     { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
    }
}

/** Maps a user role string to the corresponding navigation destination. */
private fun roleDestination(role: String): Any = when (role.lowercase()) {
    "inspector", "inspectoria" -> InspectoriaRoot
    "conductor"                -> ConductorRoot
    "admin", "administrador"   -> AdminRoot
    else                       -> InspectoriaRoot // safe default
}
