package com.tcontur.central.inspectoria.iniciar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tcontur.central.inspectoria.iniciar.qr.QrScannerView
import org.koin.compose.viewmodel.koinViewModel

// ── Paleta pastel de botones de navegación ────────────────────────────────────
private val BtnScanBg     = Color(0xFFDCEEFB)
private val BtnScanFg     = Color(0xFF1565C0)
private val BtnScanBgOff  = Color(0xFFF0F4F8)
private val BtnScanFgOff  = Color(0xFF90A4AE)

private val BtnMapaBg     = Color(0xFFE8F5E9)
private val BtnMapaFg     = Color(0xFF2E7D32)
private val BtnMapaBgOff  = Color(0xFFF0F4F8)
private val BtnMapaFgOff  = Color(0xFF90A4AE)

@Composable
fun IniciarInspeccionScreen(
    onCreated:  (Int) -> Unit,
    onBack:     () -> Unit,
    initialTab: Int = 1,
    viewModel:  IniciarInspeccionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val event by viewModel.events.collectAsState()

    LaunchedEffect(Unit) { viewModel.selectTab(initialTab) }

    LaunchedEffect(event) {
        when (val e = event) {
            is IniciarEvent.InspeccionCreada -> { viewModel.consumeEvent(); onCreated(e.id) }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()          // espacio bajo la barra de estado
    ) {

        // ── Header ─────────────────────────────────────────────────────────────
        val tabTitle = when (state.selectedTab) { 0 -> "Formulario"; 1 -> "QR"; else -> "Mapa" }

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = tabTitle,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )

            // Botón circular de retroceso
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        // ── Error ──────────────────────────────────────────────────────────────
        state.error?.let { err ->
            Snackbar(
                modifier      = Modifier.padding(8.dp),
                dismissAction = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
            ) { Text(err) }
        }

        // ── Contenido de la vista activa ───────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when (state.selectedTab) {
                0    -> FormularioTab(state, viewModel)
                1    -> QrTab(state, viewModel)
                else -> MapaTab()
            }
        }

        // ── Barra de navegación inferior ───────────────────────────────────────
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .navigationBarsPadding(),  // espacio sobre la barra de navegación del sistema
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val scanActive = state.selectedTab == 1
            val mapaActive = state.selectedTab == 2

            // Botón Scan / QR
            Button(
                onClick   = { viewModel.selectTab(1) },
                modifier  = Modifier.weight(1f).height(52.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = if (scanActive) BtnScanBg else BtnScanBgOff,
                    contentColor   = if (scanActive) BtnScanFg else BtnScanFgOff
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Scan", fontWeight = if (scanActive) FontWeight.SemiBold else FontWeight.Normal)
            }

            // Botón Mapa
            Button(
                onClick   = { viewModel.selectTab(2) },
                modifier  = Modifier.weight(1f).height(52.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = if (mapaActive) BtnMapaBg else BtnMapaBgOff,
                    contentColor   = if (mapaActive) BtnMapaFg else BtnMapaFgOff
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mapa", fontWeight = if (mapaActive) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

// ── Formulario Tab ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioTab(
    state:     IniciarInspeccionState,
    viewModel: IniciarInspeccionViewModel
) {
    var query    by remember { mutableStateOf(state.selectedUnidad?.displayText ?: "") }
    var showList by remember { mutableStateOf(false) }
    val filtered = state.filteredUnidades

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.isLoadingUnidades) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it; showList = true; viewModel.onUnidadQueryChange(it) },
                label         = { Text("Buscar unidad") },
                placeholder   = { Text("Ej: 1023, Troncal 5…") },
                singleLine    = true,
                keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.fillMaxWidth(),
                trailingIcon  = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""; showList = false; viewModel.onUnidadQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            )

            if (showList && filtered.isNotEmpty()) {
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    LazyColumn {
                        items(filtered) { option ->
                            ListItem(
                                headlineContent = { Text(option.displayText) },
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query    = option.displayText
                                        showList = false
                                        viewModel.selectUnidad(option)
                                    }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        ProximityIndicator(state)

        // Crear inspección cuando unidad seleccionada
        if (state.selectedUnidad != null || state.isCreating) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = viewModel::crear,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = state.selectedUnidad != null && !state.isCreating,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear Inspección", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProximityIndicator(state: IniciarInspeccionState) {
    when (state.proximityStatus) {
        ProximityStatus.CHECKING -> Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Text("Verificando cercanía…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        ProximityStatus.VALID -> Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null,
                tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            Column {
                Text("Unidad cercana detectada",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium)
                state.subidaNombre?.let {
                    Text("Paradero: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
            }
        }
        ProximityStatus.INVALID -> Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Error, null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Text("No estás cerca de la unidad",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
        }
        ProximityStatus.IDLE -> {}
    }
}

// ── QR Tab ─────────────────────────────────────────────────────────────────────

@Composable
private fun QrTab(
    state:     IniciarInspeccionState,
    viewModel: IniciarInspeccionViewModel
) {
    if (state.qrData == null) {
        // ── Scanner activo ──────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            key(state.qrScanKey) {
                QrScannerView(
                    onQrScanned = viewModel::onQrScanned,
                    onError     = { viewModel.clearError() },
                    modifier    = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Apunta la cámara al código QR del conductor",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        // ── Resultado del escaneo ───────────────────────────────────────────────
        val qr = state.qrData
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            // Icono de éxito
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCodeScanner, null,
                    tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
            }

            Text(
                text       = "QR escaneado",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF2E7D32)
            )

            // Card de datos del QR
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QrInfoRow("Unidad",     qr.unidad.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    QrInfoRow("Subida",     qr.subida.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    QrInfoRow("Salida",     qr.salida.toString())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    QrInfoRow("Versión QR", "v${qr.version}")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    QrInfoRow("Cortes",     "${qr.cortes.size} boleto(s)")
                }
            }

            // Botón Crear Inspección
            Button(
                onClick  = viewModel::crear,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                enabled  = !state.isCreating,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear Inspección", fontWeight = FontWeight.SemiBold)
                }
            }

            OutlinedButton(
                onClick  = viewModel::resetQrScan,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp)
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
        Text(label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        Text(value,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Mapa Tab ───────────────────────────────────────────────────────────────────

@Composable
private fun MapaTab() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.fillMaxSize(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier         = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                    }
                    Text("Próximamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32).copy(alpha = 0.6f))
                }
            }
        }
    }
}
