package com.tcontur.central.inspectoria.permission

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import central.composeapp.generated.resources.*
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.ui.theme.OrangeAccent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

// Flutter: Colors.orangeAccent
private val OrangeAccentColor = Color(0xFFFFAB40)

@Composable
fun LocationServiceScreen(
    onServiceEnabled: () -> Unit
) {
    val locationManager: LocationManager = koinInject()
    val scope          = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    // When the user returns from the OS GPS settings screen the lifecycle transitions
    // back to RESUMED. If GPS is now on, pop back so InspectoriaHome can re-check.
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED && locationManager.isServiceEnabled()) {
            onServiceEnabled()
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Flutter: FittedBox + Text x2 in orangeAccent 30sp bold
        Text(
            text       = "Servicio de ubicación",
            color      = OrangeAccentColor,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )
        Text(
            text       = "DESACTIVADO",
            color      = OrangeAccentColor,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text      = "Para continuar, active su ubicación",
            fontSize  = 14.sp,
            color     = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Flutter: Image(mylocation.png) full width
        Image(
            painter            = painterResource(Res.drawable.mylocation),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(50.dp))

        // Flutter: ElevatedButton orangeAccent, rounded 20, full width — opens GPS settings
        Button(
            onClick  = { scope.launch { locationManager.openLocationSettings() } },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = OrangeAccent,
                contentColor   = Color.White
            )
        ) {
            Text(
                text     = "Activar Ubicación",
                fontSize = 16.sp,
                color    = Color.White
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}
