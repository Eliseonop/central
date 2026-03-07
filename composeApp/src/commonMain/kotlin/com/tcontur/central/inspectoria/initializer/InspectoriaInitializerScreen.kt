package com.tcontur.central.inspectoria.initializer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import central.composeapp.generated.resources.Res
import central.composeapp.generated.resources.ic_launcher_playstore
import com.tcontur.central.ui.theme.TconturBlue
import com.tcontur.central.ui.theme.TconturBlueDark
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InspectoriaInitializerScreen(
    onConnected: () -> Unit,
    viewModel: InspectoriaInitializerViewModel = koinViewModel()
) {
    val event by viewModel.events.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        if (event is InspectoriaInitializerEvent.NavigateToHome) {
            viewModel.consumeEvent()
            onConnected()
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TconturBlueDark, TconturBlue))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(Res.drawable.ic_launcher_playstore),
                contentDescription = null,
                modifier           = Modifier.size(120.dp)
            )

            Spacer(Modifier.height(40.dp))

            BouncingDots()
        }
    }
}

@Composable
private fun BouncingDots() {
    val transition = rememberInfiniteTransition(label = "dots")

    // Cada punto tiene un delay escalonado de 200ms
    val scales = listOf(0, 200, 400).map { delayMs ->
        transition.animateFloat(
            initialValue  = 0.4f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                animation  = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(delayMs)
            ),
            label = "dot_$delayMs"
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        scales.forEach { scaleState ->
            val scale by scaleState
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(scale)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
            )
        }
    }
}
