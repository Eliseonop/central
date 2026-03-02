package com.tcontur.central.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.presentation.auth.components.CompanyDropdown
import com.tcontur.central.ui.components.AppButton
import com.tcontur.central.ui.components.AppTextField
import com.tcontur.central.ui.theme.TconturBlue
import com.tcontur.central.ui.theme.TconturBlueDark
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess(event.user.cargo)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TconturBlueDark, TconturBlue)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text       = "TCONTUR",
                color      = Color.White,
                fontSize   = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text     = "Inspector",
                color    = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(40.dp))

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text       = "Iniciar Sesión",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Company selector
                    CompanyDropdown(
                        empresas          = uiState.empresas,
                        selected          = uiState.selectedEmpresa,
                        onEmpresaSelected = viewModel::onEmpresaSelected,
                        isLoading         = uiState.isLoadingEmpresas
                    )

                    // Username
                    AppTextField(
                        value         = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        label         = "Usuario",
                        placeholder   = "correo@empresa.com"
                    )

                    // Password
                    AppTextField(
                        value                = uiState.password,
                        onValueChange        = viewModel::onPasswordChange,
                        label                = "Contraseña",
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    // Remember Me
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = uiState.rememberMe,
                            onCheckedChange = viewModel::onRememberMeChange
                        )
                        Text("Recordarme")
                    }

                    // Login button
                    AppButton(
                        text      = "Ingresar",
                        onClick   = viewModel::login,
                        isLoading = uiState.isLoggingIn
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text  = "v1.0.0",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }

    // Error dialog
    uiState.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title            = { Text("Error") },
            text             = { Text(msg) },
            confirmButton    = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }
}
