package com.tcontur.central.inspectoria.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.core.utils.toDecimalStr
import com.tcontur.central.inspectoria.InspectoriaDrawer
import com.tcontur.central.ui.theme.TconturAccent
import com.tcontur.central.ui.theme.TconturAppBar
import com.tcontur.central.ui.theme.TconturBlue
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val ColorBlue   = Color(0xFF1565C0)
private val ColorRed    = Color(0xFFC62828)
private val ColorAmber  = Color(0xFFF57F17)
private val ColorGreen  = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectoriaDashboardScreen(
    onIniciar: () -> Unit,
    onContinuar: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: InspectoriaDashboardViewModel = koinViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val event       by viewModel.events.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    LaunchedEffect(event) {
        when (val e = event) {
            is InspectoriaDashboardEvent.LoggedOut -> {
                viewModel.consumeEvent(); onLogout()
            }
            is InspectoriaDashboardEvent.NavigateToIniciar -> {
                viewModel.consumeEvent(); onIniciar()
            }
            is InspectoriaDashboardEvent.NavigateToInspeccion -> {
                viewModel.consumeEvent(); onContinuar(e.id)
            }
            null -> Unit
        }
    }

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            InspectoriaDrawer(
                user           = state.user,
                onNavigateHome = { scope.launch { drawerState.close() } },
                onLogout       = viewModel::logout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inspectoría", color = Color.White, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TconturAppBar)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Saludo ────────────────────────────────────────────────────
                state.user?.let { user ->
                    Text(
                        text       = "Hola, ${user.nombre}",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                }

                // ── Indicador de carga ────────────────────────────────────────
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // ── Error ─────────────────────────────────────────────────────
                state.error?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // ── Grid de estadísticas (2 x 2) ──────────────────────────────
                val inspeccionesCount = state.inspeccionesHoy.size

                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InspStatCard(
                        modifier    = Modifier.weight(1f),
                        label       = "Inspecciones",
                        value       = "$inspeccionesCount",
                        accentColor = ColorBlue
                    )
                    InspStatCard(
                        modifier    = Modifier.weight(1f),
                        label       = "Pasajeros Vivos",
                        value       = "S/ ${state.totalPasajerosMonto.toDecimalStr()}",
                        accentColor = ColorRed
                    )
                }

                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InspStatCard(
                        modifier    = Modifier.weight(1f),
                        label       = "Reintegros",
                        value       = "S/ ${state.totalReintegrosMonto.toDecimalStr()}",
                        accentColor = ColorAmber
                    )
                    InspStatCard(
                        modifier    = Modifier.weight(1f),
                        label       = "Última Bajada",
                        value       = state.ultimaBajada ?: "–",
                        accentColor = ColorGreen
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Inspección activa card ─────────────────────────────────────
                state.inspPendiente?.let { insp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(containerColor = ColorAmber.copy(alpha = 0.1f)),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, ColorAmber)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Inspección en curso",
                                fontWeight = FontWeight.SemiBold,
                                color      = ColorAmber,
                                fontSize   = 14.sp
                            )
                            Text(
                                "PAD ${insp.padron ?: "?"} – ${insp.placa ?: ""}",
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onSurface
                            )
                            insp.subida?.let { Text("Subida: $it", fontSize = 12.sp, color = Color.Gray) }
                        }
                    }
                }

                // ── Botón principal ────────────────────────────────────────────
                val hasPending = state.inspPendiente != null
                Button(
                    onClick  = viewModel::onInspeccionarClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (hasPending) ColorAmber else TconturBlue
                    )
                ) {
                    Text(
                        text       = if (hasPending) "Continuar inspección" else "Inspeccionar",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InspStatCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
                    .align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = label,
                fontSize   = 12.sp,
                color      = Color.Gray,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text       = value,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = accentColor,
                lineHeight = 24.sp
            )
        }
    }
}
