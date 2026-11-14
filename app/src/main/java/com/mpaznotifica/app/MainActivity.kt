package com.mpaznotifica.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mpaznotifica.app.navigation.AppNavigation
import com.mpaznotifica.app.ui.theme.MpazNotificaTheme

/**
 * Actividad única de la app (launchMode = singleTop en el manifiesto).
 *
 * Si se abre desde la notificación de un comunicado, guarda su id en
 * [comunicadoPendienteId]; AppNavigation lo atiende cuando hay sesión
 * iniciada y navega al detalle.
 */
class MainActivity : ComponentActivity() {

    private var comunicadoPendienteId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solo en el primer arranque: al rotar la pantalla el Intent
        // original sigue ahí y reabriría el detalle otra vez.
        if (savedInstanceState == null) {
            comunicadoPendienteId = NotificacionLocal.comunicadoIdDe(intent)
        }

        setContent {
            MpazNotificaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        comunicadoPendienteId = comunicadoPendienteId,
                        onComunicadoPendienteAtendido = {
                            comunicadoPendienteId = null
                        }
                    )
                }
            }
        }
    }

    // La app ya estaba abierta y se tocó una notificación.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificacionLocal.comunicadoIdDe(intent)?.let {
            comunicadoPendienteId = it
        }
    }
}
