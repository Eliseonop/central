package com.tcontur.central.inspectoria.inspeccion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.tcontur.central.core.utils.toDecimalStr
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.inspectoria.inspeccion.tabs.*
import com.tcontur.central.ui.components.LoadingOverlay
import com.tcontur.central.ui.theme.TconturAppBar
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspeccionScreen(
    inspId: Int,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    viewModel: InspeccionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()

    LaunchedEffect(inspId) { viewModel.loadInspeccion(inspId) }

    LaunchedEffect(event) {
        when (event) {
            is InspeccionEvent.Finalized, is InspeccionEvent.Cancelled -> {
                viewModel.consumeEvent(); onFinished()
            }

            null -> Unit
        }
    }

    val insp = state.inspeccion
    val tabTitles = listOf("Cortes", "Cobros", "Ocurrencias")

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (insp != null) "Insp. PAD ${insp.padron ?: "?"} (${insp.placa ?: "?"})"
                            else "Inspección",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TconturAppBar)
                )
            },
            bottomBar = {
                InspeccionBottomBar(state, viewModel)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    )
            ) {
                TabRow(selectedTabIndex = state.selectedTab) {
                    tabTitles.forEachIndexed { i, title ->
                        Tab(
                            selected = state.selectedTab == i,
                            onClick = { viewModel.selectTab(i) },
                            text = { Text(title, fontSize = 13.sp) }
                        )
                    }
                }

                state.error?.let { err ->
                    Snackbar(
                        modifier = Modifier.padding(8.dp),
                        dismissAction = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
                    ) { Text(err) }
                }

                // ── Tab content ───────────────────────────────────────────────
                when (state.selectedTab) {
                    0 -> CortesTab(
                        cortes = state.cortes,
                        ticketera = insp?.ticketera ?: false,
                        viewModel = viewModel
                    )

                    1 -> CobrosTab(state = state, viewModel = viewModel)
                    2 -> OcurrenciasTab(state = state, viewModel = viewModel)
                }
            }
        }

        if (state.isLoading) LoadingOverlay()
    }

    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showCancelDialog(false) },
            title = { Text("Cancelar Inspección") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica el motivo de cancelación:")
                    OutlinedTextField(
                        value = state.cancelMotivo,
                        onValueChange = viewModel::setCancelMotivo,
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::cancelar,
                    enabled = state.cancelMotivo.isNotBlank()
                ) { Text("Cancelar inspección", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCancelDialog(false) }) { Text("Volver") }
            }
        )
    }

    if (state.showFinalizeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showFinalizeDialog(false) },
            title = { Text("Finalizar Inspección") },
            text = { Text("¿Confirmas la finalización de esta inspección? Se registrarán los datos actuales.") },
            confirmButton = {
                TextButton(onClick = viewModel::finalizar) {
                    Text("Finalizar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showFinalizeDialog(false) }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun InspeccionBottomBar(
    state: InspeccionState,
    viewModel: InspeccionViewModel
) {
    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TotalChip(
                    label = "Reintegros",
                    count = state.totalReintegros,
                    monto = state.totalReintegrosMonto,
                    color = Color(0xFFF57F17)
                )
                TotalChip(
                    label = "Pasajeros",
                    count = state.totalPasajeros,
                    monto = state.totalPasajerosMonto,
                    color = Color(0xFFC62828)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.showCancelDialog(true) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar", fontSize = 14.sp)
                }

                Button(
                    onClick = { viewModel.showFinalizeDialog(true) },
                    modifier = Modifier.weight(2f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Finalizar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalChip(
    label: String,
    count: Int,
    monto: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(
            text = "$count  |  S/ ${monto.toDecimalStr()}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
