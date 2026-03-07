package com.tcontur.central.inspectoria.inspeccion.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.tcontur.central.inspectoria.inspeccion.InspeccionState
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel


@Composable
fun CobrosTab(
    state: InspeccionState,
    viewModel: InspeccionViewModel
) {
    val subtabTitles = listOf("Reintegros", "Pasajeros Vivos")

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(selectedTabIndex = state.selectedCobrosTab) {
            subtabTitles.forEachIndexed { i, title ->
                Tab(
                    selected = state.selectedCobrosTab == i,
                    onClick  = { viewModel.selectCobrosTab(i) },
                    text     = { Text(title, fontSize = 13.sp) }
                )
            }
        }

        // ── Sub-tab content ───────────────────────────────────────────────────
        when (state.selectedCobrosTab) {
            0 -> ReintegrosTab(cortes = state.cortes, viewModel = viewModel)
            1 -> PasajerosTab(cortes  = state.cortes, viewModel = viewModel)
        }
    }
}
