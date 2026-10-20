package com.mpaznotifica.app.ui.screen.apoderados

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.ui.components.SelectorDesplegable
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Apoderados (destinatarios) con sus pupilos y su canal preferente de despacho. Criterio de
 * éxito del caso: la emisión simulada se hace al canal preferente del apoderado.
 *
 * Pendiente con el CITT: si existe una pantalla para configurar el canal o se usa un valor fijo.
 * TODO(equipo): actualizarCanalPreferente() todavía no guarda el cambio.
 * Solo datos ficticios.
 */
@Composable
fun ApoderadosScreen(navController: NavHostController, viewModel: MpazNotificaViewModel) {
    val apoderados by viewModel.apoderados.collectAsState()

    Scaffold(topBar = { BarraSuperior("Apoderados", navController) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(apoderados) { apoderado ->
                var canal by remember(apoderado.id, apoderado.canalPreferente) { mutableStateOf(apoderado.canalPreferente) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(apoderado.nombreCompleto, style = MaterialTheme.typography.titleSmall)
                        Text("${apoderado.correo} · ${apoderado.telefono}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Pupilos: " + viewModel.nombresPupilos(apoderado).joinToString(", ").ifBlank { "sin pupilos" },
                            style = MaterialTheme.typography.bodySmall
                        )
                        SelectorDesplegable(
                            etiqueta = "Canal preferente",
                            opciones = CanalDespacho.entries,
                            seleccion = canal,
                            textoDe = { it.etiqueta },
                            onSeleccion = { canal = it }
                        )
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canal != apoderado.canalPreferente,
                            onClick = { viewModel.actualizarCanalPreferente(apoderado.id, canal) }
                        ) { Text("Guardar canal") }
                    }
                }
            }
        }
    }
}
