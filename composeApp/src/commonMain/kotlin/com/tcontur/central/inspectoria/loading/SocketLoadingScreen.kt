package com.tcontur.central.inspectoria.loading

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SocketLoadingScreen(
    onConnected: () -> Unit,
    viewModel: SocketLoadingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Consume navigation event
    LaunchedEffect(event) {
        if (event is SocketLoadingEvent.NavigateToHome) {
            viewModel.consumeEvent()
            onConnected()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = state.isConnected, label = "socket_loading") { connected ->
            if (connected) {
                SuccessContent()
            } else {
                LoadingContent()
            }
        }
    }
}


@Composable
private fun LoadingContent() {
    val alpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "alpha"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier    = Modifier.size(52.dp).alpha(alpha),
            strokeWidth = 4.dp,
            color       = Color.White
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text       = "Conectando...",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "Estableciendo conexión con el servidor",
            fontSize = 14.sp,
            color    = Color.White.copy(alpha = 0.6f)
        )
    }
}


@Composable
private fun SuccessContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Check,
            contentDescription = null,
            tint               = Color.Green,
            modifier           = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text       = "¡Conexión exitosa!",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Green
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "Cargando...",
            fontSize = 14.sp,
            color    = Color.White.copy(alpha = 0.6f)
        )
    }
}
