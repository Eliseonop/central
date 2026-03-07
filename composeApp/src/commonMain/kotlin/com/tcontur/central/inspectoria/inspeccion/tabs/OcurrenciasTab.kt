package com.tcontur.central.inspectoria.inspeccion.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.domain.inspectoria.OcurrenciaItem
import com.tcontur.central.inspectoria.inspeccion.InspeccionState
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcurrenciasTab(
    state: InspeccionState,
    viewModel: InspeccionViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Surface(shadowElevation = 4.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nueva Ocurrencia", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

                // Motivo
                OutlinedTextField(
                    value         = state.newOcurrenciaMotivo,
                    onValueChange = viewModel::setOcurrenciaMotivo,
                    label         = { Text("Motivo (mín. 10 caracteres)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    maxLines      = 4,
                    isError       = state.newOcurrenciaMotivo.isNotEmpty() && state.newOcurrenciaMotivo.length < 10,
                    supportingText = {
                        if (state.newOcurrenciaMotivo.isNotEmpty() && state.newOcurrenciaMotivo.length < 10) {
                            Text("${state.newOcurrenciaMotivo.length}/10", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }
                )

                var cargoExpanded = false
                ExposedDropdownMenuBox(
                    expanded        = false,
                    onExpandedChange = {}
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Conductor", "Cobrador").forEach { cargo ->
                            FilterChip(
                                selected = state.newOcurrenciaCargo == cargo,
                                onClick  = { viewModel.setOcurrenciaCargo(cargo) },
                                label    = { Text(cargo) }
                            )
                        }
                    }
                }

                // Add button
                Button(
                    onClick  = viewModel::agregarOcurrencia,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    enabled  = state.newOcurrenciaMotivo.length >= 10,
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar Ocurrencia", color = Color.White)
                }
            }
        }

        if (state.ocurrencias.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin ocurrencias registradas", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.ocurrencias) { ocurrencia ->
                    OcurrenciaCard(ocurrencia = ocurrencia, onEliminar = { viewModel.eliminarOcurrencia(ocurrencia.id) })
                }
            }
        }
    }
}

@Composable
private fun OcurrenciaCard(
    ocurrencia: OcurrenciaItem,
    onEliminar: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "#${ocurrencia.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp,
                        color      = Color(0xFFC62828)
                    )
                    Text(
                        text  = ocurrencia.cargo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(ocurrencia.motivo, fontSize = 13.sp, lineHeight = 18.sp)
                ocurrencia.falta?.let {
                    Text("Falta: $it", fontSize = 12.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFC62828))
            }
        }
    }
}
