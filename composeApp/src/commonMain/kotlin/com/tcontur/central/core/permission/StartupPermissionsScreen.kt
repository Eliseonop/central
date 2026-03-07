package com.tcontur.central.core.permission

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.ui.theme.OrangeAccent

private const val TAG = "[TCONTUR][PERMS_SCREEN]"

@Composable
fun StartupPermissionsScreen(onAllGranted: () -> Unit) {

    val perms = rememberStartupPermissions()

    LaunchedEffect(perms.allGranted) {
        if (perms.allGranted) {
            println("$TAG ✅ Todos los permisos concedidos — navegando a Splash")
            onAllGranted()
        }
    }

    if (!perms.allGranted) {
        println("$TAG ⚠️ Mostrando pantalla de permisos al usuario")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "Permisos necesarios",
                color      = OrangeAccent,
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text      = "La aplicación necesita acceso a tu ubicación y permiso para mostrar notificaciones.",
                fontSize  = 14.sp,
                color     = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text      = "Estos permisos son necesarios para el rastreo GPS y el estado de conexión en segundo plano.",
                fontSize  = 13.sp,
                color     = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    println("$TAG 👆 Usuario presionó 'Conceder permisos'")
                    perms.request()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text       = "Conceder permisos",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
