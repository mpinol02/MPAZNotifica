
package com.mpaznotifica.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * Segundo recurso nativo de Android.
 *
 * Muestra una notificación local cuando se registra
 * correctamente un comunicado en Room.
 *
 * No envía mensajes reales a los apoderados.
 */
object NotificacionLocal {

    private const val CANAL_ID = "mpaz_comunicados"

    private const val CANAL_NOMBRE =
        "Comunicados registrados"

    /**
     * Crea el canal de notificaciones requerido
     * por Android 8.0 o superior.
     */
    private fun crearCanal(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val canal = NotificationChannel(
                CANAL_ID,
                CANAL_NOMBRE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Avisos de comunicados guardados en MPAZ Notifica"
            }

            val administrador =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            administrador.createNotificationChannel(canal)
        }
    }

    /**
     * Muestra una notificación después de guardar
     * exitosamente un comunicado.
     *
     * En Android 13+, comprueba que el usuario
     * haya concedido el permiso correspondiente.
     */
    fun mostrarComunicadoRegistrado(
        context: Context,
        asunto: String
    ) {

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {

                val permiso = context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                )

                if (permiso != PackageManager.PERMISSION_GRANTED) {
                    Log.d(
                        "MPAZ_NOTIFICACION",
                        "Permiso de notificaciones no concedido"
                    )
                    return
                }
            }

            crearCanal(context)

            val administrador =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            val mensaje =
                "Tu comunicado \"$asunto\" se guardó " +
                        "correctamente en el historial."

            val constructor =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    Notification.Builder(
                        context,
                        CANAL_ID
                    )

                } else {

                    @Suppress("DEPRECATION")
                    Notification.Builder(context)
                }

            val notificacion = constructor
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Comunicado registrado")
                .setContentText(mensaje)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(mensaje)
                )
                .setAutoCancel(true)
                .build()

            // Identificador distinto para cada aviso.
            val idNotificacion =
                (System.currentTimeMillis() and
                        0x7FFFFFFF).toInt()

            administrador.notify(
                idNotificacion,
                notificacion
            )

        } catch (e: Exception) {

            // Una notificación fallida no debe
            // deshacer un comunicado ya guardado.
            Log.e(
                "MPAZ_NOTIFICACION",
                "No se pudo mostrar la notificación",
                e
            )
        }
    }
}
