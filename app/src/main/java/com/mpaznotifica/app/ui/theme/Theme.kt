
package com.mpaznotifica.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Configuración visual de MPAZ Notifica.
 *
 * Utiliza Material 3 y una paleta de colores
 * definida para la versión académica del proyecto.
 *
 * Colores actuales:
 * - Azul MPAZ: color principal de navegación y botones.
 * - Naranjo: color secundario para elementos destacados.
 *
 * La paleta actual es una propuesta del equipo.
 * No se presenta como identidad institucional oficial.
 *
 * Si el establecimiento o el CITT proporciona una
 * paleta oficial, deberá actualizarse esta configuración
 * junto con los recursos de res/values/colors.xml.
 *
 * Se mantiene compatibilidad con modo claro y oscuro.
 */

// =====================================================
// COLORES DEL PROYECTO
// =====================================================

private val AzulMPAZ = Color(0xFF0D47A1)

private val NaranjoAlerta = Color(0xFFEF6C00)

// =====================================================
// ESQUEMA DE COLORES CLARO
// =====================================================

private val LightColors = lightColorScheme(
    primary = AzulMPAZ,
    secondary = NaranjoAlerta,
    tertiary = NaranjoAlerta
)

// =====================================================
// ESQUEMA DE COLORES OSCURO
// =====================================================

private val DarkColors = darkColorScheme(
    primary = AzulMPAZ,
    secondary = NaranjoAlerta,
    tertiary = NaranjoAlerta
)

/**
 * Aplica el tema Material 3 a las pantallas.
 *
 * @param darkTheme Activa el esquema oscuro
 * según la configuración del dispositivo.
 *
 * @param dynamicColor Permite utilizar colores
 * dinámicos de Android cuando está habilitado.
 * Por defecto se mantiene la paleta del proyecto.
 *
 * @param content Contenido Compose al que se aplica el tema.
 */
@Composable
fun MpazNotificaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // Seleccionar el esquema de colores.
    val colorScheme = when {

        dynamicColor &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColors

        else -> LightColors
    }

    // =====================================================
    // BARRA DE ESTADO DE ANDROID
    // =====================================================

    val view = LocalView.current

    if (!view.isInEditMode) {

        SideEffect {

            val window = (view.context as Activity).window

            // Mantener el color institucional
            // en la barra de estado.
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()

            // Conserva el comportamiento previo del proyecto.
            WindowCompat.getInsetsController(
                window,
                view
            ).isAppearanceLightStatusBars = darkTheme
        }
    }

    // =====================================================
    // APLICAR MATERIAL 3
    // =====================================================

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
