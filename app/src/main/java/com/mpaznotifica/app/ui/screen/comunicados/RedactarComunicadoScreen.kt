
package com.mpaznotifica.app.ui.screen.comunicados

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpaznotifica.app.NotificacionLocal
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.ErroresComunicado
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.ui.components.SelectorDesplegable
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Pantalla de redacción y envío de comunicados.
 *
 * Incluye:
 * - Campos obligatorios y validaciones.
 * - Tipos y alcances según el perfil.
 * - Destinos permitidos por RF-06.
 * - Selector nativo de PDF.
 * - Aviso animado de firma física.
 * - Confirmación antes del envío.
 * - Guardado mediante el ViewModel y Room.
 * - Notificación local al guardar exitosamente.
 */
@Composable
fun RedactarComunicadoScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel
) {

    val context = LocalContext.current

    val usuario by viewModel.usuarioActivo.collectAsState()

    var asunto by remember {
        mutableStateOf("")
    }

    var cuerpo by remember {
        mutableStateOf("")
    }

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

    var enviando by remember {
        mutableStateOf(false)
    }

    var errorEnvio by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()

    // =====================================================
    // PRIMER RECURSO NATIVO: SELECTOR DE ARCHIVOS PDF
    // =====================================================

    val selectorPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->

        if (uri != null) {
            adjunto = uri.lastPathSegment ?: "documento.pdf"
        }
    }

    // =====================================================
    // SEGUNDO RECURSO NATIVO: NOTIFICACIÓN LOCAL
    // =====================================================

    /*
     * Guardamos el asunto temporalmente cuando
     * necesitamos solicitar el permiso de notificaciones.
     */
    var asuntoParaNotificacion by remember {
        mutableStateOf("")
    }

    /*
     * Android 13 o superior solicita permiso
     * para mostrar notificaciones.
     */
    val permisoNotificacion =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {

                NotificacionLocal.mostrarComunicadoRegistrado(
                    context = context,
                    asunto = asuntoParaNotificacion
                )

            } else {

                Log.d(
                    "MPAZ_NOTIFICACION",
                    "El usuario no concedió permiso"
                )
            }
        }

    /*
     * Solo mostramos la notificación cuando
     * Room confirma que el comunicado se guardó.
     */
    fun notificarGuardado(asuntoGuardado: String) {

        asuntoParaNotificacion = asuntoGuardado

        val requierePermiso =
            Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.TIRAMISU

        if (requierePermiso) {

            val concedido =
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (concedido) {

                NotificacionLocal.mostrarComunicadoRegistrado(
                    context = context,
                    asunto = asuntoGuardado
                )

            } else {

                permisoNotificacion.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

        } else {

            NotificacionLocal.mostrarComunicadoRegistrado(
                context = context,
                asunto = asuntoGuardado
            )
        }
    }

    // =====================================================
    // INTERFAZ DE REDACCIÓN
    // =====================================================

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

            // -------------------------------------------------
            // ASUNTO
            // -------------------------------------------------

            OutlinedTextField(
                value = asunto,
                onValueChange = {
                    asunto = it
                    errores = errores.copy(
                        asunto = null
                    )
                },
                label = {
                    Text("Asunto")
                },
                singleLine = true,
                isError = errores.asunto != null,
                supportingText = if (
                    errores.asunto != null
                ) {
                    {
                        Text(
                            errores.asunto.orEmpty()
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            // -------------------------------------------------
            // CUERPO DEL MENSAJE
            // -------------------------------------------------

            OutlinedTextField(
                value = cuerpo,
                onValueChange = {
                    cuerpo = it
                    errores = errores.copy(
                        cuerpo = null
                    )
                },
                label = {
                    Text("Cuerpo del mensaje")
                },
                minLines = 4,
                isError = errores.cuerpo != null,
                supportingText = if (
                    errores.cuerpo != null
                ) {
                    {
                        Text(
                            errores.cuerpo.orEmpty()
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )

            // -------------------------------------------------
            // TIPO DE COMUNICACIÓN
            // -------------------------------------------------

            SelectorDesplegable(
                etiqueta = "Tipo de comunicación",
                opciones = viewModel.tiposPermitidos(usuario),
                seleccion = tipo,
                textoDe = {
                    it.etiqueta
                },
                onSeleccion = {
                    tipo = it
                    errores = errores.copy(
                        tipo = null
                    )
                },
                error = errores.tipo
            )

            // -------------------------------------------------
            // ALCANCE SEGÚN PERFIL
            // -------------------------------------------------

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

            // -------------------------------------------------
            // DESTINOS AUTORIZADOS POR RF-06
            // -------------------------------------------------

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
                        errores = errores.copy(
                            destino = null
                        )
                    },
                    error = errores.destino
                )
            }

            // -------------------------------------------------
            // SELECTOR PDF NATIVO
            // -------------------------------------------------

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
                        if (adjunto == null) {
                            "Adjuntar PDF (opcional)"
                        } else {
                            "Cambiar PDF"
                        }
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
                    text = "Adjunto: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // -------------------------------------------------
            // FIRMA FÍSICA
            // -------------------------------------------------

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

            // -------------------------------------------------
            // ANIMACIÓN JETPACK COMPOSE - G7
            // -------------------------------------------------

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
                        text =
                            "Rol solo informativo: la app notifica y avisa " +
                                    "que la autorización debe firmarse en papel. " +
                                    "No se capturan firmas en la app.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // -------------------------------------------------
            // BOTÓN ENVIAR
            // -------------------------------------------------

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

    // =====================================================
    // CONFIRMACIÓN DE ENVÍO
    // =====================================================

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
                    "Una vez enviado, el comunicado queda " +
                            "registrado en el historial y no se puede editar."
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

                                val asuntoGuardado = asunto.trim()

                                val guardado = try {

                                    viewModel.enviarComunicado(
                                        asunto = asuntoGuardado,
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

                                    Log.e(
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

                                    // El comunicado ya está en Room.
                                    enviado = true

                                    // Nuevo segundo recurso nativo.
                                    notificarGuardado(
                                        asuntoGuardado
                                    )

                                } else {

                                    errorEnvio = true
                                }
                            }
                        }
                    }
                ) {

                    Text(
                        if (enviando) {
                            "Guardando..."
                        } else {
                            "Enviar"
                        }
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

    // =====================================================
    // COMUNICADO GUARDADO
    // =====================================================

    if (enviado) {

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Comunicado enviado")
            },
            text = {

                Text(
                    "El comunicado y los resultados del " +
                            "despacho simulado se guardaron correctamente."
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

    // =====================================================
    // ERROR DE GUARDADO
    // =====================================================

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
