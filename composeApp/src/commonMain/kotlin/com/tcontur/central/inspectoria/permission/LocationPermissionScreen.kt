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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import central.composeapp.generated.resources.*
import com.tcontur.central.core.location.rememberRequestLocationPermission
import com.tcontur.central.ui.theme.OrangeAccent
import org.jetbrains.compose.resources.painterResource

// Flutter: Colors.blueAccent
private val BlueAccentColor = Color(0xFF448AFF)

@Composable
fun LocationPermissionScreen(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    val requestPermission = rememberRequestLocationPermission { granted ->
        if (granted) onPermissionGranted() else onPermissionDenied()
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Flutter: FittedBox + Text x2 in blueAccent 30sp bold
        Text(
            text       = "Permiso de ubicación",
            color      = BlueAccentColor,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )
        Text(
            text       = "Requerido",
            color      = BlueAccentColor,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text      = "Para continuar, necesitamos tu permiso de ubicación.",
            fontSize  = 14.sp,
            color     = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Flutter: Image(confirmation.png) full width
        Image(
            painter            = painterResource(Res.drawable.confirmation),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(50.dp))

        // Flutter: ElevatedButton orangeAccent, rounded 20, full width
        Button(
            onClick  = requestPermission,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = OrangeAccent,
                contentColor   = Color.White
            )
        ) {
            Text(
                text     = "Permitir acceso a la ubicación",
                fontSize = 16.sp,
                color    = Color.White
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}
