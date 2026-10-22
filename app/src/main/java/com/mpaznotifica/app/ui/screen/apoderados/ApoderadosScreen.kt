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
 * Pantalla de apoderados y canales preferentes de despacho.
 *
 * Permite consultar los datos ficticios de cada apoderado,
 * incluyendo su nombre, correo, teléfono y estudiantes asociados.
 *
 * El usuario puede seleccionar y guardar el canal preferente
 * de comunicación: correo electrónico, SMS o WhatsApp.
 *
 * Persistencia:
 * - actualizarCanalPreferente() solicita el cambio al ViewModel.
 * - ApoderadoRepository guarda el canal seleccionado en Room.
 * - StateFlow y collectAsState() actualizan la interfaz.
 *
 * El despacho de comunicados es simulado.
 * No se envían mensajes reales a los apoderados.
 *
 * La configuración del canal preferente se implementó
 * como funcionalidad del proyecto utilizando datos ficticios.
 * Su disponibilidad por perfil puede requerir confirmación
 * con el CITT.
 */
@Composable
fun ApoderadosScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel
) {

    // Observar los apoderados desde el ViewModel.
    // Los cambios guardados en Room actualizan esta lista.
    val apoderados by viewModel.apoderados.collectAsState()

    Scaffold(
        topBar = {
            BarraSuperior(
                "Apoderados",
                navController
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(apoderados) { apoderado ->

                // Estado local de la selección.
                // Se sincroniza con el canal observado desde Room.
                var canal by remember(
                    apoderado.id,
                    apoderado.canalPreferente
                ) {
                    mutableStateOf(
                        apoderado.canalPreferente
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // Nombre del apoderado.
                        Text(
                            text = apoderado.nombreCompleto,
                            style = MaterialTheme.typography.titleSmall
                        )

                        // Datos de contacto ficticios.
                        Text(
                            text = "${apoderado.correo} · ${apoderado.telefono}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Estudiantes relacionados con el apoderado.
                        Text(
                            text = "Pupilos: " +
                                    viewModel.nombresPupilos(apoderado)
                                        .joinToString(", ")
                                        .ifBlank { "sin pupilos" },
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Seleccionar el canal preferente.
                        SelectorDesplegable(
                            etiqueta = "Canal preferente",
                            opciones = CanalDespacho.entries,
                            seleccion = canal,
                            textoDe = {
                                it.etiqueta
                            },
                            onSeleccion = {
                                canal = it
                            }
                        )

                        // Guardar el cambio mediante el ViewModel.
                        // El botón solo está habilitado cuando
                        // el canal seleccionado es diferente
                        // del que actualmente está guardado.
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canal != apoderado.canalPreferente,
                            onClick = {
                                viewModel.actualizarCanalPreferente(
                                    apoderado.id,
                                    canal
                                )
                            }
                        ) {

                            Text("Guardar canal")
                        }
                    }
                }
            }
        }
    }
}
