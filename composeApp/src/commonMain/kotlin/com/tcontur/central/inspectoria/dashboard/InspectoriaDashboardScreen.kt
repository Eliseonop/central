package com.tcontur.central.inspectoria.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tcontur.central.core.utils.toDecimalStr
import com.tcontur.central.inspectoria.InspectoriaDrawer
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Paleta pastel de tarjetas ──────────────────────────────────────────────────
private val CardBlueBg  = Color(0xFFDCEEFB)
private val CardBlueFg  = Color(0xFF1565C0)
private val CardRedBg   = Color(0xFFFFEBEE)
private val CardRedFg   = Color(0xFFC62828)
private val CardGoldBg  = Color(0xFFFFF8E1)
private val CardGoldFg  = Color(0xFFBF6A00)
private val CardGreenBg = Color(0xFFE8F5E9)
private val CardGreenFg = Color(0xFF2E7D32)

// ── Paleta pastel de botones ───────────────────────────────────────────────────
private val BtnScanBg  = Color(0xFFDCEEFB)
private val BtnScanFg  = Color(0xFF1565C0)
private val BtnMapaBg  = Color(0xFFE8F5E9)
private val BtnMapaFg  = Color(0xFF2E7D32)

@Composable
fun InspectoriaDashboardScreen(
    onIniciar:     () -> Unit,
    onIniciarMapa: () -> Unit,
    onContinuar:   (Int) -> Unit,
    onLogout:      () -> Unit,
    viewModel:     InspectoriaDashboardViewModel = koinViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val event       by viewModel.events.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    LaunchedEffect(event) {
        when (val e = event) {
            is InspectoriaDashboardEvent.LoggedOut            -> { viewModel.consumeEvent(); onLogout() }
            is InspectoriaDashboardEvent.NavigateToIniciar    -> { viewModel.consumeEvent(); onIniciar() }
            is InspectoriaDashboardEvent.NavigateToInspeccion -> { viewModel.consumeEvent(); onContinuar(e.id) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()         // espacio bajo la barra de estado
        ) {

            // ── Header ─────────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = "Inspectoría",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    state.user?.let {
                        Text(
                            text  = "Hola, ${it.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                // Avatar circular → abre drawer
                Box(
                    modifier         = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { scope.launch { drawerState.open() } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint               = MaterialTheme.colorScheme.onPrimary,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // ── Contenido principal ────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }

                // ── Grid 2 × 2 ────────────────────────────────────────────────
                val inspeccionesCount = state.inspeccionesHoy.size

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Inspecciones",
                        value    = "$inspeccionesCount",
                        icon     = Icons.Default.AssignmentTurnedIn,
                        bgColor  = CardBlueBg,
                        fgColor  = CardBlueFg
                    )
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Pasajeros",
                        value    = "S/ ${state.totalPasajerosMonto.toDecimalStr()}",
                        icon     = Icons.Default.Group,
                        bgColor  = CardRedBg,
                        fgColor  = CardRedFg
                    )
                }

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Reintegros",
                        value    = "S/ ${state.totalReintegrosMonto.toDecimalStr()}",
                        icon     = Icons.Default.SwapHoriz,
                        bgColor  = CardGoldBg,
                        fgColor  = CardGoldFg
                    )
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label    = "Última Bajada",
                        value    = state.ultimaBajada ?: "–",
                        icon     = Icons.Default.AccessTime,
                        bgColor  = CardGreenBg,
                        fgColor  = CardGreenFg
                    )
                }

                // ── Card de inspección en curso ────────────────────────────────
                state.inspPendiente?.let { insp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = CardGoldBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier         = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CardGoldFg.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AssignmentTurnedIn, null,
                                    tint = CardGoldFg, modifier = Modifier.size(20.dp))
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Inspección en curso",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = CardGoldFg
                                )
                                Text(
                                    "PAD ${insp.padron ?: "?"} – ${insp.placa ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                insp.subida?.let {
                                    Text("Subida: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            // ── Barra de acciones inferior ─────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding(),  // espacio sobre la barra de navegación
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Scan
                Button(
                    onClick  = viewModel::onInspeccionarClick,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = BtnScanBg,
                        contentColor   = BtnScanFg
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = if (state.inspPendiente != null) "Continuar" else "Scan",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón Mapa
                Button(
                    onClick   = onIniciarMapa,
                    modifier  = Modifier.weight(1f).height(52.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(
                        containerColor = BtnMapaBg,
                        contentColor   = BtnMapaFg
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mapa", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Tarjeta estadística ────────────────────────────────────────────────────────

@Composable
private fun DashStatCard(
    modifier: Modifier,
    label:    String,
    value:    String,
    icon:     ImageVector,
    bgColor:  Color,
    fgColor:  Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icono circular
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(fgColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = fgColor,
                    modifier           = Modifier.size(20.dp)
                )
            }
            // Etiqueta + valor
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = fgColor.copy(alpha = 0.7f)
                )
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = fgColor
                )
            }
        }
    }
}
