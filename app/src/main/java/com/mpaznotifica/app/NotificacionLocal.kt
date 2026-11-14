
package com.mpaznotifica.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * Segundo recurso nativo de Android.
 *
 * Muestra una notificación local cuando se registra
 * correctamente un comunicado en Room.
 *
 * Al tocar la notificación se abre la app directamente en el
 * detalle de ese comunicado (si no hay sesión iniciada, primero
 * se pide el login y luego se muestra el detalle).
 *
 * No envía mensajes reales a los apoderados.
 */
object NotificacionLocal {

    private const val CANAL_ID = "mpaz_comunicados"

    private const val CANAL_NOMBRE =
        "Comunicados registrados"

    /** Extra del Intent con el id del comunicado que debe abrirse. */
    const val EXTRA_COMUNICADO_ID =
        "com.mpaznotifica.app.EXTRA_COMUNICADO_ID"

    /**
     * Intent que abre MainActivity llevando el id del comunicado.
     * MainActivity es singleTop: si la app ya está abierta recibe
     * el Intent en onNewIntent() en lugar de crearse otra vez.
     */
    fun crearIntentDetalle(
        context: Context,
        comunicadoId: String
    ): Intent {

        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_COMUNICADO_ID, comunicadoId)
        }
    }

    /** Lee el id del comunicado desde un Intent, o null si no lo trae. */
    fun comunicadoIdDe(intent: Intent?): String? {
        return intent
            ?.getStringExtra(EXTRA_COMUNICADO_ID)
            ?.takeIf { it.isNotBlank() }
    }

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
     *
     * [comunicadoId] es el id con el que se guardó el comunicado:
     * al tocar la notificación se abre su detalle.
     */
    fun mostrarComunicadoRegistrado(
        context: Context,
        asunto: String,
        comunicadoId: String
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

            // requestCode propio de cada comunicado: si fuera el mismo,
            // Android reutilizaría el PendingIntent del aviso anterior
            // y abriría el detalle equivocado.
            val abrirDetalle = PendingIntent.getActivity(
                context,
                comunicadoId.hashCode(),
                crearIntentDetalle(context, comunicadoId),
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

            val notificacion = constructor
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Comunicado registrado")
                .setContentText(mensaje)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(mensaje)
                )
                .setContentIntent(abrirDetalle)
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
