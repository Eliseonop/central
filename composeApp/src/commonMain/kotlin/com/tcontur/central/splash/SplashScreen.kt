package com.tcontur.central.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tcontur.central.ui.theme.TconturBlue
import com.tcontur.central.ui.theme.TconturBlueDark
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRole: (String) -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var visible by remember { mutableStateOf(false) }

    // Fade-in animation for logo text
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) { visible = true }

    // React to ViewModel state changes
    LaunchedEffect(state) {
        when (val s = state) {
            is SplashState.Authenticated  -> onNavigateToRole(s.role)
            is SplashState.Unauthenticated -> onNavigateToLogin()
            SplashState.Loading            -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(TconturBlueDark, TconturBlue))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "TCONTUR",
            color     = androidx.compose.ui.graphics.Color.White,
            fontSize  = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier  = Modifier.alpha(alpha)
        )
    }
}
