package com.tcontur.central.inspectoria.inspeccion.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcontur.central.core.utils.toDecimalStr
import com.tcontur.central.domain.inspectoria.CorteItem
import com.tcontur.central.inspectoria.inspeccion.InspeccionViewModel
import kotlinx.coroutines.delay

@Composable
fun CortesTab(
    cortes: List<CorteItem>,
    ticketera: Boolean,
    viewModel: InspeccionViewModel
) {
    // Show active cards (mostrar=true) AND terminated cards (terminado=true, red bg).
    // Cards that are hidden/pending (mostrar=false, terminado=false) are not displayed.
    val visible = cortes.filter { it.mostrar || it.terminado }

    if (visible.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin tickets disponibles", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(visible, key = { it.boletoId * 1_000_000 + it.inicio }) { corte ->
            val realIndex = cortes.indexOf(corte)
            if (corte.terminado && !corte.mostrar) {
                // ── Terminated card (red, with restore icon) ─────────────────
                TerminadoCard(
                    corte = corte,
                    onReestablecer = { viewModel.reestablecerCorte(realIndex) }
                )
            } else {
                // ── Active card (input on the right) ─────────────────────────
                ActiveCorteCard(
                    corte = corte,
                    ticketera = ticketera,
                    onNumeroChange = { num -> viewModel.updateCorteNumero(realIndex, num) },
                    onTerminar = { viewModel.terminarCorte(realIndex) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Active card
//
//  [LEFT: flex-grow]                  [RIGHT: fixed]
//  Row1: nombre  serie                 ticketera → numero (text)
//  Row2: tarifa  inicio–fin            else      → [SkipNext icon?] + input (88dp)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveCorteCard(
    corte: CorteItem,
    ticketera: Boolean,
    onNumeroChange: (Int) -> Unit,
    onTerminar: () -> Unit
) {
    val accent = parseColor(corte.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── LEFT: nombre + serie / tarifa + rango ─────────────────────────
            Column(modifier = Modifier.weight(1f)) {

                // Row 1: nombre  serie  (always visible)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = corte.nombre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (corte.serie.isNotEmpty()) {
                        Text(
                            text = corte.serie,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = accent
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Row 2: tarifa  inicio–fin  (always visible)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S/ ${corte.tarifa.toDecimalStr()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Text(
                        text = "${formatTicket(corte.inicio)} – ${formatTicket(corte.fin)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (ticketera) {
                Text(
                    text = formatTicket(corte.numero),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (corte.quedan) {
                        IconButton(
                            onClick = onTerminar,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Terminar suministro",
                                tint = accent
                            )
                        }
                    }
                    var tfv by remember(corte.boletoId, corte.inicio) {
                        val formatted = formatTicket(corte.numero)
                        mutableStateOf(TextFieldValue(formatted))
                    }
                    var isFocused by remember { mutableStateOf(false) }

                    LaunchedEffect(isFocused) {
                        if (isFocused) {
                            delay(50)
                            val t = tfv.text
                            val selStart = (t.length - 3).coerceAtLeast(0)
                            tfv = tfv.copy(selection = TextRange(selStart, t.length))
                        }
                    }

                    fun commitValue() {
                        val num = tfv.text.toIntOrNull() ?: corte.inicio
                        val clamped = num.coerceIn(corte.inicio, corte.fin)
                        val formatted = formatTicket(clamped)
                        tfv = TextFieldValue(formatted, selection = TextRange(formatted.length))
                        onNumeroChange(clamped)
                    }

                    OutlinedTextField(
                        value = tfv,
                        onValueChange = { v ->
                            val filtered = v.text.filter { it.isDigit() }
                            if (filtered.length <= 6) {
                                tfv = v.copy(text = filtered)
                            }
                        },
                        modifier = Modifier
                            .width(112.dp)
                            .onFocusChanged { fs ->
                                if (fs.isFocused) {
                                    isFocused = true
                                } else if (isFocused) {
                                    isFocused = false
                                    commitValue()   // format + notify ViewModel on blur
                                }
                            },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { commitValue() }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminadoCard(
    corte: CorteItem,
    onReestablecer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "S/ ${corte.tarifa.toDecimalStr()}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = corte.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFFC62828)
                )
                if (corte.serie.isNotEmpty()) {
                    Text(
                        text = corte.serie,
                        fontSize = 12.sp,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Text(
                text = "${formatTicket(corte.inicio)} – ${formatTicket(corte.fin)}",
                fontSize = 11.sp,
                color = Color(0xFFC62828)
            )

            IconButton(
                onClick = onReestablecer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Reestablecer suministro",
                    tint = Color(0xFFC62828)
                )
            }
        }
    }
}


private fun formatTicket(number: Int): String =
    number.toString().padStart(6, '0')

internal fun parseColor(name: String): Color = when (name.lowercase()) {
    "red", "rojo" -> Color(0xFFC62828)
    "blue", "azul" -> Color(0xFF1565C0)
    "green", "verde" -> Color(0xFF2E7D32)
    "yellow", "amarillo" -> Color(0xFFF9A825)
    "orange", "naranja" -> Color(0xFFE65100)
    "purple", "morado" -> Color(0xFF6A1B9A)
    "pink", "rosado" -> Color(0xFFAD1457)
    "teal", "turquesa" -> Color(0xFF00695C)
    else -> Color(0xFF1565C0)
}
