
package com.mpaznotifica.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpaznotifica.app.navigation.Rutas
import com.mpaznotifica.app.ui.components.BarraSuperior
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

/**
 * Panel principal de MPAZ Notifica.
 *
 * Se muestra después de iniciar sesión correctamente.
 *
 * Funcionalidades:
 * - Muestra el nombre del usuario autenticado.
 * - Identifica el perfil activo.
 * - Muestra el curso asignado al Profesor Jefe.
 * - Indica la cantidad de comunicados registrados.
 * - Permite acceder a la redacción de comunicados.
 * - Permite consultar el historial y su trazabilidad.
 * - Permite consultar apoderados y configurar su canal.
 *
 * La información del usuario y de los comunicados
 * se obtiene desde MpazNotificaViewModel utilizando
 * StateFlow y collectAsState().
 *
 * Permisos:
 * - Inspectoría General puede enviar comunicados
 *   según los alcances definidos para su perfil.
 * - Profesor Jefe solamente puede enviar a su curso
 *   y a apoderados individuales de ese curso.
 *
 * Estas restricciones se validan en el ViewModel
 * y no dependen únicamente de los botones del panel.
 *
 * Decisión funcional actual:
 * Ambos perfiles pueden acceder al módulo de apoderados.
 * Si el CITT establece una restricción adicional,
 * deberá aplicarse también en la navegación y la
 * lógica correspondiente.
 */
@Composable
fun PanelScreen(
    navController: NavHostController,
    viewModel: MpazNotificaViewModel,
    onCerrarSesion: () -> Unit
) {

    // =====================================================
    // ESTADO OBSERVADO DESDE EL VIEWMODEL
    // =====================================================

    // Usuario que inició sesión.
    val usuario by viewModel.usuarioActivo.collectAsState()

    // Comunicados guardados en Room.
    // El contador se actualiza al recibir nuevos datos.
    val comunicados by viewModel.comunicados.collectAsState()

    // =====================================================
    // INTERFAZ DEL PANEL PRINCIPAL
    // =====================================================

    Scaffold(
        topBar = {
            BarraSuperior(
                "MPAZ Notifica",
                navController,
                onCerrarSesion
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // -------------------------------------------------
            // BIENVENIDA
            // -------------------------------------------------

            Text(
                text = "Hola, ${usuario?.nombre.orEmpty()}",
                style = MaterialTheme.typography.titleLarge
            )

            // -------------------------------------------------
            // RESUMEN DEL USUARIO Y COMUNICADOS
            // -------------------------------------------------

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // Perfil de Inspectoría General o Profesor Jefe.
                    Text(
                        text = "Perfil: ${usuario?.perfil?.etiqueta.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Mostrar curso de jefatura cuando corresponda.
                    usuario?.cursoAsignado?.let { curso ->

                        Text(
                            text = "Curso de jefatura: $curso",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Cantidad de comunicados registrados en Room.
                    Text(
                        text = "Comunicados en la bitácora: ${comunicados.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // -------------------------------------------------
            // ACCESO A REDACTAR COMUNICADO
            // -------------------------------------------------

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(
                        Rutas.REDACTAR
                    )
                }
            ) {

                Text("Redactar comunicado")
            }

            // -------------------------------------------------
            // ACCESO AL HISTORIAL
            // -------------------------------------------------

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(
                        Rutas.HISTORIAL
                    )
                }
            ) {

                Text("Historial de comunicados")
            }

            // -------------------------------------------------
            // ACCESO A APODERADOS Y CANAL PREFERENTE
            // -------------------------------------------------

            /*
             * Este módulo está disponible actualmente
             * para ambos perfiles de la aplicación.
             *
             * Los permisos de envío RF-06 se comprueban
             * de manera independiente en el ViewModel.
             */
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(
                        Rutas.APODERADOS
                    )
                }
            ) {

                Text("Apoderados y canal preferente")
            }
        }
    }
}
