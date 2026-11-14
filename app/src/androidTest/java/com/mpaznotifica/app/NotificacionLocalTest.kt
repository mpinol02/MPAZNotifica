package com.mpaznotifica.app

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas del Intent que abre el detalle desde la notificación.
 *
 * Se prueba el Intent (no la notificación en pantalla) para que
 * no dependa del permiso POST_NOTIFICATIONS del emulador.
 */
@RunWith(AndroidJUnit4::class)
class NotificacionLocalTest {

    private val context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun intentDelDetalleLlevaElIdDelComunicado() {

        val intent = NotificacionLocal.crearIntentDetalle(context, "COM-123")

        assertEquals(
            "COM-123",
            NotificacionLocal.comunicadoIdDe(intent)
        )

        assertEquals(
            MainActivity::class.java.name,
            intent.component?.className
        )
    }

    @Test
    fun intentAbreLaAppSinDuplicarLaActividad() {

        val intent = NotificacionLocal.crearIntentDetalle(context, "COM-123")

        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }

    @Test
    fun cadaComunicadoGeneraSuPropioIntent() {

        val uno = NotificacionLocal.crearIntentDetalle(context, "COM-1")
        val dos = NotificacionLocal.crearIntentDetalle(context, "COM-2")

        assertNotEquals(
            NotificacionLocal.comunicadoIdDe(uno),
            NotificacionLocal.comunicadoIdDe(dos)
        )
    }

    @Test
    fun intentSinIdNoAbreNingunDetalle() {

        assertNull(NotificacionLocal.comunicadoIdDe(Intent()))
        assertNull(NotificacionLocal.comunicadoIdDe(null))

        val vacio = Intent().putExtra(
            NotificacionLocal.EXTRA_COMUNICADO_ID,
            "  "
        )

        assertNull(NotificacionLocal.comunicadoIdDe(vacio))
    }
}
