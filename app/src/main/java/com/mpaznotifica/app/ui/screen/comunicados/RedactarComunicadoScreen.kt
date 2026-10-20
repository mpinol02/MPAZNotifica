
package com.mpaznotifica.app.ui.screen.comunicados

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// Agregado por el equipo: animaciones Jetpack Compose (G7).
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.ErroresComunicado
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.ui.components.SelectorDesplegable
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Módulo "Redacción y envío": asunto, cuerpo, tipo, alcance y destino, adjunto PDF opcional y
 * marca de autorización con firma física. Pide confirmación antes de enviar y muestra una
 * alerta visual al terminar (RNF del caso).
 *
 * La pantalla está completa; lo pendiente está en el ViewModel (TODO del equipo):
 *  - tiposPermitidos() / alcancesPermitidos() / destinosPara(): hoy no restringen por perfil;
 *  - validarComunicado(): hoy no valida;
 *  - enviarComunicado(): hoy no registra ni simula el despacho.
 */
@Composable
fun RedactarComunicadoScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel
) {

    val usuario by viewModel.usuarioActivo.collectAsState()

    var asunto by remember { mutableStateOf("") }
    var cuerpo by remember { mutableStateOf("") }
    var tipo by remember {
        mutableStateOf<TipoComunicacion?>(null)
    }
    var alcance by remember {
        mutableStateOf<AlcanceEnvio?>(null)
    }
    var destino by remember {
        mutableStateOf<String?>(null)
    }
    var adjunto by remember {
        mutableStateOf<String?>(null)
    }
    var requiereFirma by remember {
        mutableStateOf(false)
    }

    var errores by remember {
        mutableStateOf(ErroresComunicado())
    }

    var confirmando by remember {
        mutableStateOf(false)
    }

    var enviado by remember {
        mutableStateOf(false)
    }

    // Agregado por el equipo: estados del guardado Room.
    var enviando by remember {
        mutableStateOf(false)
    }

    var errorEnvio by remember {
        mutableStateOf(false)
    }

    // Agregado por el equipo: desplazamiento al mostrar errores.
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Selector de archivos del sistema, solo PDF (RF: adjunto PDF opcional desde el dispositivo).
    // TODO(equipo): si el CITT define un límite de tamaño, validarlo aquí.
    val selectorPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            adjunto = uri.lastPathSegment ?: "documento.pdf"
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                "Redactar comunicado",
                navController
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Asunto del comunicado.
            OutlinedTextField(
                value = asunto,
                onValueChange = {
                    asunto = it
                    errores = errores.copy(asunto = null)
                },
                label = {
                    Text("Asunto")
                },
                singleLine = true,
                isError = errores.asunto != null,
                supportingText = if (errores.asunto != null) {
                    {
                        Text(errores.asunto.orEmpty())
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Cuerpo del comunicado.
            OutlinedTextField(
                value = cuerpo,
                onValueChange = {
                    cuerpo = it
                    errores = errores.copy(cuerpo = null)
                },
                label = {
                    Text("Cuerpo del mensaje")
                },
                minLines = 4,
                isError = errores.cuerpo != null,
                supportingText = if (errores.cuerpo != null) {
                    {
                        Text(errores.cuerpo.orEmpty())
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo de comunicación.
            SelectorDesplegable(
                etiqueta = "Tipo de comunicación",
                opciones = viewModel.tiposPermitidos(usuario),
                seleccion = tipo,
                textoDe = {
                    it.etiqueta
                },
                onSeleccion = {
                    tipo = it
                    errores = errores.copy(tipo = null)
                },
                error = errores.tipo
            )

            // Alcance permitido según el perfil.
            SelectorDesplegable(
                etiqueta = "Alcance (destinatarios)",
                opciones = viewModel.alcancesPermitidos(usuario),
                seleccion = alcance,
                textoDe = {
                    it.etiqueta
                },
                onSeleccion = {
                    alcance = it
                    destino = null
                    errores = errores.copy(
                        alcance = null,
                        destino = null
                    )
                },
                error = errores.alcance
            )

            // Destinos según perfil y alcance.
            if (
                alcance != null &&
                alcance != AlcanceEnvio.COLEGIO_COMPLETO
            ) {
                SelectorDesplegable(
                    etiqueta = "Destino",
                    opciones = viewModel.destinosPara(
                        alcance,
                        usuario
                    ),
                    seleccion = destino,
                    textoDe = {
                        it
                    },
                    onSeleccion = {
                        destino = it
                        errores = errores.copy(destino = null)
                    },
                    error = errores.destino
                )
            }

            // Recurso nativo: adjuntar PDF.
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        selectorPdf.launch(
                            arrayOf("application/pdf")
                        )
                    }
                ) {
                    Text(
                        if (adjunto == null)
                            "Adjuntar PDF (opcional)"
                        else
                            "Cambiar PDF"
                    )
                }

                if (adjunto != null) {
                    TextButton(
                        onClick = {
                            adjunto = null
                        }
                    ) {
                        Text("Quitar")
                    }
                }
            }

            adjunto?.let {
                Text(
                    "Adjunto: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Autorización informativa con firma física.
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = requiereFirma,
                    onCheckedChange = {
                        requiereFirma = it
                    }
                )

                Text(
                    "Es una autorización que requiere " +
                            "firma física del apoderado"
                )
            }

            // Agregado por el equipo: animación de entrada
            // y salida del aviso de firma física (rúbrica G7).
            //
            // fadeIn/fadeOut: aparición y desaparición suave.
            // expandVertically/shrinkVertically: expansión
            // y contracción del espacio del aviso.
            AnimatedVisibility(
                visible = requiereFirma,
                enter = fadeIn(
                    animationSpec = tween(300)
                ) + expandVertically(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(
                    animationSpec = tween(250)
                ) + shrinkVertically(
                    animationSpec = tween(250)
                )
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Rol solo informativo: la app notifica y avisa " +
                                "que la autorización debe firmarse en papel. " +
                                "No se capturan firmas en la app.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Validar antes de mostrar la confirmación.
            Button(
                enabled = !enviando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {

                    errores = viewModel.validarComunicado(
                        asunto = asunto,
                        cuerpo = cuerpo,
                        tipo = tipo,
                        alcance = alcance,
                        destino = destino
                    )

                    if (errores.hayErrores) {

                        coroutineScope.launch {
                            scrollState.animateScrollTo(0)
                        }

                    } else if (
                        tipo != null &&
                        alcance != null
                    ) {
                        confirmando = true
                    }
                }
            ) {
                Text("Enviar")
            }
        }
    }

    // Confirmación antes de enviar (el comunicado queda registrado de forma inmutable).
    if (confirmando) {
        AlertDialog(
            onDismissRequest = {
                if (!enviando) {
                    confirmando = false
                }
            },
            title = {
                Text("¿Enviar comunicado?")
            },
            text = {
                Text(
                    "Una vez enviado, el comunicado queda registrado " +
                            "en el historial y no se puede editar."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !enviando,
                    onClick = {

                        val tipoElegido = tipo
                        val alcanceElegido = alcance

                        if (
                            tipoElegido != null &&
                            alcanceElegido != null &&
                            !enviando
                        ) {

                            enviando = true

                            coroutineScope.launch {

                                val guardado = try {
                                    viewModel.enviarComunicado(
                                        asunto = asunto.trim(),
                                        cuerpo = cuerpo.trim(),
                                        tipo = tipoElegido,
                                        alcance = alcanceElegido,
                                        destino = destino,
                                        adjuntoPdfNombre = adjunto,
                                        requiereFirmaFisica = requiereFirma
                                    )
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    android.util.Log.e(
                                        "MPAZ_ROOM",
                                        "Error durante el envío",
                                        e
                                    )
                                    false
                                } finally {
                                    enviando = false
                                }

                                confirmando = false

                                if (guardado) {
                                    enviado = true
                                } else {
                                    errorEnvio = true
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (enviando)
                            "Guardando..."
                        else
                            "Enviar"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !enviando,
                    onClick = {
                        confirmando = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Alerta visual de confirmación de envío (RNF del caso).
    if (enviado) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Comunicado enviado")
            },
            text = {
                Text(
                    "El comunicado y los resultados del despacho " +
                            "simulado se guardaron correctamente."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        enviado = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Mensaje agregado por el equipo: error de guardado.
    if (errorEnvio) {
        AlertDialog(
            onDismissRequest = {
                errorEnvio = false
            },
            title = {
                Text("No se pudo guardar")
            },
            text = {
                Text(
                    "Comprueba los datos, destinatarios y permisos " +
                            "del usuario. Inténtalo nuevamente."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorEnvio = false
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
