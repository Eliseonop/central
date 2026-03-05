package com.tcontur.central.inspectoria.inspeccion.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tcontur.central.core.utils.toDecimalStr
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.domain.inspectoria.CorteItem
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel

@Composable
fun ReintegrosTab(
    cortes: List<CorteItem>,
    viewModel: InspeccionViewModel
) {
    if (cortes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin tickets disponibles", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(cortes.filter { it.mostrar }) { _, corte ->
            val realIndex = cortes.indexOf(corte)
            ContadorCard(
                corte     = corte,
                value     = corte.reintegros,
                onInc     = { viewModel.incrementarReintegro(realIndex) },
                onDec     = { viewModel.decrementarReintegro(realIndex) }
            )
        }
    }
}

@Composable
internal fun ContadorCard(
    corte: CorteItem,
    value: Int,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    val accentColor = parseColor(corte.color)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(64.dp)
                    .background(accentColor, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(corte.nombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(corte.serie, fontSize = 12.sp, color = Color.Gray)
                    Text("S/ ${corte.tarifa.toDecimalStr()}", fontSize = 12.sp, color = accentColor)
                }
            }

            // Counter
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.padding(end = 12.dp)
            ) {
                IconButton(
                    onClick  = onDec,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEF9A9A), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Text(
                    text       = "$value",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.widthIn(min = 28.dp),
                    color      = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick  = onInc,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF81C784), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Más", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
