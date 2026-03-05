package com.tcontur.central.inspectoria.home

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.tcontur.central.inspectoria.InspectoriaDrawer
import com.tcontur.central.inspectoria.WebViewContent
import com.tcontur.central.ui.theme.TconturAppBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectoriaHomeScreen(
    onLogout: () -> Unit,
    viewModel: InspectoriaHomeViewModel = koinViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val event       by viewModel.events.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

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
                user           = state.user,
                onNavigateHome = { scope.launch { drawerState.close() } },
                onLogout       = viewModel::logout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Bus Inspector - TCONTUR", color = Color.White, fontSize = 20.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint               = Color.White
                            )
                        }
                    },
                    // Flutter AppBar color: #125183
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TconturAppBar)
                )
            }
        ) { innerPadding ->
            // Socket is guaranteed to be connected when we arrive here
            // (SocketLoadingScreen waited for it). Show WebView as soon as the URL is ready.
            if (state.webUrl.isNotBlank()) {
                WebViewContent(
                    url      = state.webUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    onError  = { }
                )
            }
        }
    }
}
