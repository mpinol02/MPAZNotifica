
package com.mpaznotifica.app.ui.screen.comunicados

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpaznotifica.app.AdjuntoPdf
import com.mpaznotifica.app.model.EstadoEnvio
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Detalle de un comunicado del historial. RF: "cada registro debe incluir autor, fecha/hora,
 * contenido, destinatarios y canal empleado" y "registrar el estado del envío por destinatario".
 *
 * Regla de negocio: el comunicado es INMUTABLE, por eso esta pantalla es solo de lectura
 * (no tiene botones de editar ni borrar).
 */
@Composable
fun DetalleComunicadoScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel
) {

    val context = LocalContext.current

    val seleccionadoId by
    viewModel.comunicadoSeleccionadoId.collectAsState()

    val comunicados by
    viewModel.comunicados.collectAsState()

    val comunicado = comunicados.find {
        it.id == seleccionadoId
    }

    // Integración del equipo: observar los envíos desde Room.
    val enviosFlow = remember(comunicado?.id) {
        comunicado?.id?.let { id ->
            viewModel.observarEnvios(id)
        }
    }

    val envios by (
            enviosFlow ?: remember {
                kotlinx.coroutines.flow.flowOf(
                    emptyList<com.mpaznotifica.app.model.EnvioDestinatario>()
                )
            }
            ).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            BarraSuperior(
                "Detalle del comunicado",
                navController
            )
        }
    ) { padding ->

        if (comunicado == null) {
            Text(
                "Comunicado no encontrado.",
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        comunicado.asunto,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        comunicado.cuerpo,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text("Tipo: ${comunicado.tipo.etiqueta}")

                    Text(
                        "Alcance: ${comunicado.alcance.etiqueta}" +
                                (comunicado.destinoReferencia?.let {
                                    " ($it)"
                                } ?: "")
                    )

                    Text("Autor: ${comunicado.autorNombre}")
                    Text("Fecha y hora: ${comunicado.fechaHora}")

                    Text(
                        "Adjunto: " +
                                (comunicado.adjuntoPdfNombre ?: "sin adjunto")
                    )

                    // Persistencia del PDF: se abre la copia guardada en la app.
                    if (comunicado.adjuntoPdfNombre != null) {

                        val ruta = comunicado.adjuntoPdfRuta

                        if (ruta != null) {

                            OutlinedButton(
                                onClick = {

                                    val mensaje = when (
                                        AdjuntoPdf.abrir(context, ruta)
                                    ) {
                                        AdjuntoPdf.ResultadoApertura.ABIERTO ->
                                            null

                                        AdjuntoPdf.ResultadoApertura.SIN_VISOR ->
                                            "No hay una app instalada para abrir PDF."

                                        AdjuntoPdf.ResultadoApertura.ARCHIVO_NO_DISPONIBLE ->
                                            "El archivo PDF ya no está disponible."
                                    }

                                    if (mensaje != null) {
                                        Toast.makeText(
                                            context,
                                            mensaje,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            ) {
                                Text("Abrir PDF")
                            }

                        } else {

                            Text(
                                "El archivo no se guardó en este dispositivo " +
                                        "(comunicado anterior).",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (comunicado.requiereFirmaFisica) {
                        Text(
                            "Autorización con firma física: " +
                                    "la firma se entrega en papel.",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Text(
                "Destinatarios y estado del envío (${envios.size})",
                style = MaterialTheme.typography.titleSmall
            )

            if (envios.isEmpty()) {
                Text(
                    "Sin registros de envío.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            envios.forEach { envio ->

                val apoderado =
                    viewModel.apoderadoPorId(envio.apoderadoId)

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            apoderado?.nombreCompleto ?: envio.apoderadoId,
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            "${envio.canal.etiqueta} · ${envio.estado.etiqueta}",
                            color = if (
                                envio.estado == EstadoEnvio.FALLIDO
                            ) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
