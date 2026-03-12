package com.tcontur.central.core.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import com.tcontur.central.core.session.SessionManager
import com.tcontur.central.core.socket.SocketDispatcherViewModel
import com.tcontur.central.domain.auth.UserRole
import com.tcontur.central.inspectoria.initializer.InspectoriaInitializerScreen
import com.tcontur.central.inspectoria.inspectoriaNavGraph
import com.tcontur.central.login.LoginScreen
import com.tcontur.central.splash.SplashScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = StartupPermissions  // ← permissions gate is always first
) {
    val sessionManager: SessionManager = koinInject()
    val scope = rememberCoroutineScope()

    // Activate the central socket message dispatcher at app scope.
    // Being called here (outside any NavBackStackEntry) it is scoped to the
    // Activity ViewModelStore — it lives for the full user session and is
    // never garbage-collected mid-navigation.
    @Suppress("UNUSED_VARIABLE")
    val socketDispatcher: SocketDispatcherViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        SessionEventBus.unauthorized.collect {
            // Full session shutdown: stop location service + disconnect socket + clear storage
            sessionManager.logout()
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
                onNavigateToRole = { role: UserRole ->
                    navController.navigate(RoleRouter(role)) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────────
        composable<Login> {
            LoginScreen(
                onLoginSuccess = { role: UserRole ->
                    navController.navigate(RoleRouter(role)) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        // ── Role Router ───────────────────────────────────────────────────────
        // Invisible routing composable: reads the role and immediately navigates
        // to the correct initializer or root. No UI is shown.
        composable<RoleRouter> { backStackEntry ->
            val args = backStackEntry.toRoute<RoleRouter>()
            LaunchedEffect(Unit) {
                val dest = when (args.role) {
                    UserRole.INSPECTORIA -> InspectoriaInitializer   // needs socket + GPS
                    UserRole.CONDUCTOR   -> ConductorRoot             // no socket/GPS
                    UserRole.ADMIN       -> AdminRoot                 // no socket/GPS
                    UserRole.UNKNOWN     -> InspectoriaInitializer    // safe default
                }
                navController.navigate(dest) {
                    popUpTo(RoleRouter(args.role)) { inclusive = true }
                }
            }
        }

        // ── Inspectoria initializer ───────────────────────────────────────────
        // Inspectoria-only: fetches empresa, connects socket, authenticates, starts GPS.
        composable<InspectoriaInitializer> {
            InspectoriaInitializerScreen(
                onConnected = {
                    navController.navigate(InspectoriaRoot) {
                        popUpTo(InspectoriaInitializer) { inclusive = true }
                    }
                }
            )
        }

        // ── Inspectoria role graph ─────────────────────────────────────────────
        inspectoriaNavGraph(
            navController = navController,
            onLogout = {
                // Full session shutdown: stop location service + disconnect socket + clear storage
                scope.launch {
                    sessionManager.logout()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )

        // ── Other role graphs (stubs) ──────────────────────────────────────────
        composable<ConductorRoot> { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
        composable<AdminRoot>     { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Módulo en construcción") } }
    }
}

