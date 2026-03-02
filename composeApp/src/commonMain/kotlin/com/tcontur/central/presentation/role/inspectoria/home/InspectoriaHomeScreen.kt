package com.tcontur.central.presentation.role.inspectoria.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.tcontur.central.presentation.role.inspectoria.components.InspectoriaDrawer
import com.tcontur.central.presentation.role.inspectoria.webview.WebViewContent
import com.tcontur.central.ui.components.LoadingOverlay
import com.tcontur.central.ui.theme.TconturBlue
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectoriaHomeScreen(
    onLogout: () -> Unit,
    onRequestLocationPermission: (NavController) -> Unit,
    onRequestLocationService: (NavController) -> Unit,
    viewModel: InspectoriaHomeViewModel = koinViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val event       by viewModel.events.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    // Handle one-shot events from ViewModel
    LaunchedEffect(event) {
        when (event) {
            InspectoriaHomeEvent.LoggedOut -> {
                viewModel.consumeEvent()
                onLogout()
            }
            else -> Unit
        }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            InspectoriaDrawer(
                user            = state.user,
                onNavigateHome  = { scope.launch { drawerState.close() } },
                onLogout        = viewModel::logout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title  = { Text("Bus Inspector – TCONTUR", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TconturBlue)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (state.webUrl.isNotBlank()) {
                    WebViewContent(
                        url       = state.webUrl,
                        modifier  = Modifier.fillMaxSize(),
                        onError   = { /* Show error UI */ }
                    )
                } else {
                    LoadingOverlay()
                }
            }
        }
    }
}
