
package com.mpaznotifica.app.ui.screen.comunicados

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.model.Usuario
import com.mpaznotifica.app.navigation.Rutas
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.ui.components.SelectorDesplegable
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Pantalla de historial y trazabilidad de MPAZ Notifica.
 *
 * Muestra los comunicados registrados en Room y permite
 * consultar su información mediante filtros.
 *
 * Filtros disponibles:
 * - Tipo de comunicación.
 * - Emisor del comunicado.
 * - Fecha inicial.
 * - Fecha final.
 *
 * Los filtros se aplican mediante historialFiltrado()
 * del ViewModel y los resultados se ordenan desde
 * el comunicado más reciente al más antiguo.
 *
 * Al seleccionar un comunicado se abre su detalle,
 * donde se muestra:
 * - Asunto y cuerpo del mensaje.
 * - Autor y fecha de emisión.
 * - Alcance y destino.
 * - Documento PDF referenciado, cuando corresponde.
 * - Requisito de firma física.
 * - Destinatarios, canales y estados del despacho.
 *
 * Los registros se conservan utilizando Room y SQLite.
 * El despacho es simulado y no realiza envíos reales.
 */
@Composable
fun HistorialScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel
) {

    // Observar los comunicados almacenados en Room.
    // collectAsState() actualiza la pantalla cuando
    // el StateFlow emite nuevos datos.
    val todos by viewModel.comunicados.collectAsState()

    // =====================================================
    // ESTADO DE LOS FILTROS
    // =====================================================

    var tipo by remember {
        mutableStateOf<TipoComunicacion?>(null)
    }

    var emisor by remember {
        mutableStateOf<Usuario?>(null)
    }

    var desde by remember {
        mutableStateOf("")
    }

    var hasta by remember {
        mutableStateOf("")
    }

    // =====================================================
    // APLICAR FILTROS
    // =====================================================

    /*
     * La lista se recalcula cuando cambian
     * los comunicados o alguno de los filtros.
     *
     * El ViewModel contiene las reglas de filtrado
     * y ordena los resultados por fecha descendente.
     */
    val lista = remember(
        todos,
        tipo,
        emisor,
        desde,
        hasta
    ) {
        viewModel.historialFiltrado(
            tipo = tipo,
            emisorId = emisor?.id,
            fechaDesde = desde.ifBlank { null },
            fechaHasta = hasta.ifBlank { null }
        )
    }

    // =====================================================
    // INTERFAZ DEL HISTORIAL
    // =====================================================

    Scaffold(
        topBar = {
            BarraSuperior(
                "Historial de comunicados",
                navController
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // -------------------------------------------------
            // FILTRO POR TIPO
            // -------------------------------------------------

            SelectorDesplegable<TipoComunicacion?>(
                etiqueta = "Tipo",
                opciones =
                    listOf<TipoComunicacion?>(null) +
                            TipoComunicacion.entries,
                seleccion = tipo,
                textoDe = {
                    it?.etiqueta ?: "Todos los tipos"
                },
                onSeleccion = {
                    tipo = it
                },
                textoVacio = "Todos los tipos",
                modifier = Modifier.padding(top = 8.dp)
            )

            // -------------------------------------------------
            // FILTRO POR EMISOR
            // -------------------------------------------------

            SelectorDesplegable<Usuario?>(
                etiqueta = "Emisor",
                opciones =
                    listOf<Usuario?>(null) +
                            viewModel.emisores,
                seleccion = emisor,
                textoDe = {
                    it?.nombre ?: "Todos los emisores"
                },
                onSeleccion = {
                    emisor = it
                },
                textoVacio = "Todos los emisores"
            )

            // -------------------------------------------------
            // FILTROS POR FECHAS
            // -------------------------------------------------

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedTextField(
                    value = desde,
                    onValueChange = {
                        desde = it
                    },
                    label = {
                        Text("Desde (AAAA-MM-DD)")
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = hasta,
                    onValueChange = {
                        hasta = it
                    },
                    label = {
                        Text("Hasta (AAAA-MM-DD)")
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // -------------------------------------------------
            // CANTIDAD DE COMUNICADOS ENCONTRADOS
            // -------------------------------------------------

            Text(
                text = "${lista.size} comunicado(s)",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(
                    vertical = 8.dp
                )
            )

            // -------------------------------------------------
            // LISTA DE COMUNICADOS
            // -------------------------------------------------

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(lista) { comunicado ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                                // Registrar la selección
                                // antes de navegar al detalle.
                                viewModel.seleccionarComunicado(
                                    comunicado.id
                                )

                                navController.navigate(
                                    Rutas.DETALLE
                                )
                            }
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            // Asunto del comunicado.
                            Text(
                                text = comunicado.asunto,
                                style = MaterialTheme.typography.titleSmall
                            )

                            // Tipo, alcance y destino.
                            Text(
                                text =
                                    "${comunicado.tipo.etiqueta} · " +
                                            comunicado.alcance.etiqueta +
                                            (
                                                    comunicado.destinoReferencia
                                                        ?.let { " · $it" }
                                                        ?: ""
                                                    ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Aviso de autorización con firma física.
                            if (comunicado.requiereFirmaFisica) {

                                Text(
                                    text =
                                        "Requiere firma física " +
                                                "(rol informativo)",
                                    style =
                                        MaterialTheme.typography.labelSmall,
                                    color =
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }

                            // Fecha y autor del comunicado.
                            Text(
                                text =
                                    "${comunicado.fechaHora} · " +
                                            comunicado.autorNombre,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
