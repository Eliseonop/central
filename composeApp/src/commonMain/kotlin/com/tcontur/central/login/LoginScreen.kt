package com.tcontur.central.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import central.composeapp.generated.resources.*   // Res + all drawable extension properties
import com.tcontur.central.domain.auth.UserRole
import com.tcontur.central.ui.theme.TconturBlue
import com.tcontur.central.ui.theme.TconturGrad0
import com.tcontur.central.ui.theme.TconturGrad1
import com.tcontur.central.ui.theme.TconturGrad2
import com.tcontur.central.ui.theme.TconturGrad3
import com.tcontur.central.ui.theme.TconturIconBlue
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess(event.user.role)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(TconturGrad0, TconturGrad1, TconturGrad2, TconturGrad3)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter           = painterResource(Res.drawable.logo),
                contentDescription = "TCONTUR logo",
                modifier          = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            FieldLabel("Empresa")
            Spacer(Modifier.height(10.dp))
            CompanyDropdownFlutter(
                empresas   = uiState.empresas,
                selected   = uiState.selectedEmpresa?.nombre ?: "",
                isLoading  = uiState.isLoadingEmpresas,
                onSelected = viewModel::onEmpresaSelected
            )

            Spacer(Modifier.height(20.dp))

            // ── Usuario ───────────────────────────────────────────────────────
            FieldLabel("Usuario")
            Spacer(Modifier.height(10.dp))
            FlutterTextField(
                value         = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                hint          = "Usuario",
                leadingIcon   = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TconturIconBlue)
                }
            )

            Spacer(Modifier.height(20.dp))

            // ── Contraseña ────────────────────────────────────────────────────
            FieldLabel("Contraseña")
            Spacer(Modifier.height(10.dp))
            FlutterTextField(
                value                = uiState.password,
                onValueChange        = viewModel::onPasswordChange,
                hint                 = "Contraseña",
                visualTransformation = PasswordVisualTransformation('*'),
                leadingIcon          = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TconturIconBlue)
                }
            )

            Spacer(Modifier.height(10.dp))

            // ── Recordarme ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked         = uiState.rememberMe,
                    onCheckedChange = viewModel::onRememberMeChange,
                    colors          = CheckboxDefaults.colors(
                        checkedColor           = Color.White,
                        uncheckedColor         = Color.White,
                        checkmarkColor         = TconturBlue,
                        disabledCheckedColor   = Color.White.copy(alpha = 0.5f),
                        disabledUncheckedColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text       = "Recordarme",
                    color      = Color.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Botón Iniciar Sesión ───────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Button(
                onClick   = viewModel::login,
                enabled   = !uiState.isLoggingIn && uiState.selectedEmpresa != null,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp),
                shape     = RoundedCornerShape(30.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor         = Color(0xFF2196F3), // Colors.blue
                    contentColor           = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor   = Color.White.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
            ) {
                if (uiState.isLoggingIn) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text       = "Iniciar Sesión",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Versión 0.9.0", color = Color.White, fontSize = 14.sp)
        }
    }

    // Error dialog
    uiState.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title            = { Text("Error", color = Color.Red) },
            text             = { Text(msg, textAlign = TextAlign.Center) },
            confirmButton    = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text       = text,
        color      = Color.White,
        fontSize   = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier   = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FlutterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value                = value,
        onValueChange        = onValueChange,
        placeholder          = { Text(hint, color = Color.Black.copy(alpha = 0.38f)) },
        leadingIcon          = leadingIcon,
        singleLine           = true,
        visualTransformation = visualTransformation,
        textStyle            = TextStyle(color = Color.Black.copy(alpha = 0.87f), fontSize = 16.sp),
        modifier             = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp)),
        shape                = RoundedCornerShape(10.dp),
        colors               = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent,
            focusedContainerColor   = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyDropdownFlutter(
    empresas: List<com.tcontur.central.domain.Empresa>,
    selected: String,
    isLoading: Boolean,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = if (isLoading) "Cargando..." else selected,
            onValueChange = {},
            readOnly      = true,
            placeholder   = { Text("Empresa", color = Color.Black.copy(alpha = 0.38f)) },
            leadingIcon   = {
                Icon(Icons.Default.Business, contentDescription = null, tint = TconturIconBlue)
            },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine    = true,
            textStyle     = TextStyle(
                color      = Color.Black.copy(alpha = 0.87f),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier      = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .background(Color.White, RoundedCornerShape(10.dp)),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (!isLoading) {
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false },
                modifier         = Modifier.background(Color.White)
            ) {
                empresas.forEachIndexed { index, empresa ->
                    DropdownMenuItem(
                        text    = {
                            Text(empresa.nombre, color = Color.Black.copy(alpha = 0.87f))
                        },
                        onClick = {
                            onSelected(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
