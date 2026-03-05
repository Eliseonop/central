package com.tcontur.central.inspectoria.inspeccion.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tcontur.central.domain.inspectoria.CorteItem
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel

@Composable
fun PasajerosTab(
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
            // Reuse ContadorCard from ReintegrosTab, but with pasajerosVivos value
            ContadorCard(
                corte = corte,
                value = corte.pasajerosVivos,
                onInc = { viewModel.incrementarPasajero(realIndex) },
                onDec = { viewModel.decrementarPasajero(realIndex) }
            )
        }
    }
}
