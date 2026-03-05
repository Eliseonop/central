//package com.tcontur.central.login
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import com.tcontur.central.domain.Empresa
//
//@Composable
//fun CompanyDropdown(
//    empresas: List<Empresa>,
//    selected: Empresa?,
//    onEmpresaSelected: (Int) -> Unit,
//    modifier: Modifier = Modifier,
//    isLoading: Boolean = false
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Box(modifier = modifier.fillMaxWidth()) {
//        OutlinedButton(
//            onClick  = { expanded = true },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(
//                text  = selected?.nombre ?: if (isLoading) "Cargando..." else "Selecciona empresa",
//                color = MaterialTheme.colorScheme.onSurface
//            )
//        }
//
//        DropdownMenu(
//            expanded        = expanded,
//            onDismissRequest = { expanded = false },
//            modifier        = Modifier.fillMaxWidth()
//        ) {
//            empresas.forEachIndexed { index, empresa ->
//                DropdownMenuItem(
//                    text    = { Text(empresa.nombre) },
//                    onClick = {
//                        onEmpresaSelected(index)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}
