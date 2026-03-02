package com.tcontur.central.presentation.role.inspectoria.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.domain.model.User
import com.tcontur.central.ui.theme.TconturBlue

@Composable
fun InspectoriaDrawer(
    user: User?,
    onNavigateHome: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color.White)
            .padding(vertical = 24.dp)
    ) {
        // ─── Header ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TconturBlue)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar placeholder
            Column(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { }

            Spacer(Modifier.height(8.dp))
            Text(
                text  = user?.nombre ?: "",
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text  = user?.cargo ?: "",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // ─── Menu Items ───────────────────────────────────────────────
        NavigationDrawerItem(
            icon    = { Icon(Icons.Default.Home, contentDescription = null) },
            label   = { Text("Inicio") },
            selected = true,
            onClick  = onNavigateHome
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        NavigationDrawerItem(
            icon    = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            label   = { Text("Cerrar sesión") },
            selected = false,
            onClick  = { showLogoutDialog = true }
        )
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = { Text("Cerrar sesión") },
            text             = { Text("¿Deseas cerrar sesión?") },
            confirmButton    = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Sí") }
            },
            dismissButton    = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("No") }
            }
        )
    }
}
