package com.mpaznotifica.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mpaznotifica.app.ui.screen.LoginScreen
import com.mpaznotifica.app.ui.screen.PanelScreen
import com.mpaznotifica.app.ui.screen.apoderados.ApoderadosScreen
import com.mpaznotifica.app.ui.screen.comunicados.DetalleComunicadoScreen
import com.mpaznotifica.app.ui.screen.comunicados.HistorialScreen
import com.mpaznotifica.app.ui.screen.comunicados.RedactarComunicadoScreen
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel

object Rutas {
    const val LOGIN = "login"
    const val PANEL = "panel"
    const val REDACTAR = "redactar"
    const val HISTORIAL = "historial"
    const val DETALLE = "detalle"
    const val APODERADOS = "apoderados"
}

/**
 * Flujo de pantallas (un solo NavHost y un único MpazNotificaViewModel compartido):
 *
 *   LOGIN ──(login OK)──> PANEL ──> REDACTAR
 *                               ──> HISTORIAL ──> DETALLE
 *                               ──> APODERADOS (canal preferente)
 *
 * El comunicado elegido se comparte a través del ViewModel (como en la Guía 11): no se pasan
 * argumentos por la ruta.
 *
 * Botón atrás: el atrás nativo de Android funciona solo (lo maneja el NavHost). Al ingresar,
 * LOGIN sale de la pila; "Cerrar sesión" limpia la pila completa.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MpazNotificaViewModel = viewModel()
) {
    val cerrarSesion: () -> Unit = {
        viewModel.logout()
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onIngresar = {
                    navController.navigate(Rutas.PANEL) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.PANEL) {
            PanelScreen(navController, viewModel, onCerrarSesion = cerrarSesion)
        }
        composable(Rutas.REDACTAR) {
            RedactarComunicadoScreen(navController, viewModel)
        }
        composable(Rutas.HISTORIAL) {
            HistorialScreen(navController, viewModel)
        }
        composable(Rutas.DETALLE) {
            DetalleComunicadoScreen(navController, viewModel)
        }
        composable(Rutas.APODERADOS) {
            ApoderadosScreen(navController, viewModel)
        }
    }
}
