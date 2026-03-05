package com.tcontur.central.inspectoria

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import central.composeapp.generated.resources.*   // Res + all drawable extension properties
import com.tcontur.central.domain.User
import com.tcontur.central.ui.theme.TconturIconBlue
import org.jetbrains.compose.resources.painterResource

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
            .navigationBarsPadding()
    ) {
        // ─── Header (UserAccountsDrawerHeader equivalent) ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // panel.jpg background
            Image(
                painter            = painterResource(Res.drawable.panel),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.matchParentSize()
            )

            // User info overlay at bottom-left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Circular avatar — icon.png
                Image(
                    painter            = painterResource(Res.drawable.icon),
                    contentDescription = "Avatar",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.height(8.dp))
                // Empty name (matches Flutter accountName = Text(""))
                Text(text = "", color = Color.Black)
                // User nombre
                Text(
                    text  = user?.nombre ?: "-",
                    style = TextStyle(
                        color      = TconturIconBlue,  // #0066a9 @ 60%
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.W400
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ─── Inicio ────────────────────────────────────────────────────────
        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Home, contentDescription = null) },
            label    = { Text("Inicio") },
            selected = true,
            onClick  = onNavigateHome
        )

        Spacer(Modifier.height(10.dp))

        // ─── Push logout to bottom ─────────────────────────────────────────
        Spacer(Modifier.weight(1f))

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        NavigationDrawerItem(
            icon     = { Icon(Icons.Default.Logout, contentDescription = null) },
            label    = { Text("Cerrar sesión") },
            selected = false,
            onClick  = { showLogoutDialog = true }
        )

        Spacer(Modifier.height(8.dp))
    }

    // ─── Logout confirmation dialog ────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint               = Color(0xFF2196F3), // Colors.blue
                        modifier           = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Cerrar Sesión")
                }
            },
            text             = { Text("¿Estás seguro de que deseas cerrar sesión?") },
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
