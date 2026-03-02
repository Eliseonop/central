package com.tcontur.central.presentation.role

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Placeholder composable for roles not yet implemented.
 * Replace this with a real nav graph as each role is developed.
 */
@Composable
fun RoleRouter() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = "Módulo en construcción",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
