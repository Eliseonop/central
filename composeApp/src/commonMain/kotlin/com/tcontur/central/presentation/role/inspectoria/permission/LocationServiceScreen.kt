package com.tcontur.central.presentation.role.inspectoria.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tcontur.central.ui.components.AppButton

@Composable
fun LocationServiceScreen(
    onServiceEnabled: (NavController) -> Unit,
    navController: NavController? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "Servicio GPS Desactivado",
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = "El GPS de tu dispositivo está desactivado. Actívalo para que el sistema pueda registrar tu posición.",
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(40.dp))

        AppButton(
            text    = "Activar GPS",
            onClick = {
                // Platform-specific settings opener handled via LocationManager
                navController?.let { onServiceEnabled(it) }
            }
        )
    }
}
