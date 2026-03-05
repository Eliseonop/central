package com.tcontur.central.inspectoria.iniciar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.domain.inspectoria.UnidadOption
import com.tcontur.central.inspectoria.iniciar.qr.QrScannerView
import com.tcontur.central.ui.theme.TconturAppBar
import com.tcontur.central.ui.theme.TconturBlue
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IniciarInspeccionScreen(
    onCreated: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: IniciarInspeccionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()

    LaunchedEffect(event) {
        when (val e = event) {
            is IniciarEvent.InspeccionCreada -> { viewModel.consumeEvent(); onCreated(e.id) }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Inspección", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TconturAppBar)
            )
        },
        bottomBar = {
            // Show Crear button when:
            // - Formulario tab AND unidad selected
            // - QR tab AND QR scanned successfully
            val canCreate = when (state.selectedTab) {
                0    -> state.selectedUnidad != null && !state.isCreating
                1    -> state.qrData != null && !state.isCreating
                else -> false
            }
            if (canCreate || state.isCreating) {
                Surface(shadowElevation = 8.dp) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick  = viewModel::crear,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            enabled  = canCreate,
                            colors   = ButtonDefaults.buttonColors(containerColor = TconturBlue)
                        ) {
                            if (state.isCreating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Crear Inspección", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
        ) {
            // ── Tabs ──────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor   = MaterialTheme.colorScheme.surface
            ) {
                listOf("Formulario", "QR", "Mapa").forEachIndexed { i, title ->
                    Tab(
                        selected = state.selectedTab == i,
                        onClick  = { viewModel.selectTab(i) },
                        text     = { Text(title) }
                    )
                }
            }

            // ── Error snackbar ─────────────────────────────────────────────────
            state.error?.let { err ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    dismissAction = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
                ) { Text(err) }
            }

            // ── Tab content ────────────────────────────────────────────────────
            when (state.selectedTab) {
                0 -> FormularioTab(state, viewModel)
                1 -> QrTab(state, viewModel)
                2 -> MapaTab()
            }
        }
    }
}

// ── Formulario Tab ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioTab(
    state: IniciarInspeccionState,
    viewModel: IniciarInspeccionViewModel
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Unidad", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)

        if (state.isLoadingUnidades) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            ExposedDropdownMenuBox(
                expanded         = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value          = state.selectedUnidad?.displayText ?: "",
                    onValueChange  = {},
                    readOnly       = true,
                    label          = { Text("Selecciona una unidad") },
                    trailingIcon   = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier       = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.unidades.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option.displayText) },
                            onClick = { viewModel.selectUnidad(option); expanded = false }
                        )
                    }
                }
            }
        }

        // ── Proximity status ───────────────────────────────────────────────────
        when (state.proximityStatus) {
            ProximityStatus.CHECKING -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Verificando cercanía...", fontSize = 13.sp, color = Color.Gray)
            }
            ProximityStatus.VALID -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                Column {
                    Text("Unidad cercana detectada", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                    state.subidaNombre?.let { Text("Paradero: $it", fontSize = 12.sp, color = Color.Gray) }
                }
            }
            ProximityStatus.INVALID -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Text("No estás cerca de la unidad", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            }
            ProximityStatus.IDLE -> {}
        }

        // ── Ticketera ──────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = state.ticketera, onCheckedChange = viewModel::setTicketera)
            Spacer(Modifier.width(8.dp))
            Text("Usa ticketera", fontSize = 14.sp)
        }
    }
}

// ── QR Tab ─────────────────────────────────────────────────────────────────────

@Composable
private fun QrTab(
    state: IniciarInspeccionState,
    viewModel: IniciarInspeccionViewModel
) {
    if (state.qrData == null) {
        // ── Scanner active ──────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            // key() remounts the composable to restart scanning
            key(state.qrScanKey) {
                QrScannerView(
                    onQrScanned = viewModel::onQrScanned,
                    onError     = { viewModel.clearError() /* error shown in snackbar */ },
                    modifier    = Modifier.fillMaxSize()
                )
            }
            // Overlay instructions
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Apunta la cámara al código QR del conductor",
                    color    = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    } else {
        // ── Scan result ─────────────────────────────────────────────────────
        val qr = state.qrData
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint     = Color(0xFF2E7D32),
                modifier = Modifier.size(56.dp)
            )
            Text("QR escaneado exitosamente", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))

            // ── QR summary card ────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QrInfoRow("Unidad",      qr.unidad.toString())
                    QrInfoRow("Subida",      qr.subida.toString())
                    QrInfoRow("Salida",      qr.salida.toString())
                    QrInfoRow("Versión QR",  "v${qr.version}")
                    QrInfoRow("Cortes",      "${qr.cortes.size} boleto(s)")
                }
            }

            Text(
                "Presiona \"Crear Inspección\" para continuar.",
                fontSize = 13.sp,
                color    = Color.Gray
            )

            // ── Re-scan button ─────────────────────────────────────────────
            OutlinedButton(
                onClick = viewModel::resetQrScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Escanear de nuevo")
            }
        }
    }
}

@Composable
private fun QrInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Mapa Tab ───────────────────────────────────────────────────────────────────

@Composable
private fun MapaTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Mapa", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Próximamente", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
