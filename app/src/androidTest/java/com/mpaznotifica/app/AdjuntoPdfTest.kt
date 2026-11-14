package com.mpaznotifica.app

import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mpaznotifica.app.data.local.AppDatabase
import com.mpaznotifica.app.data.local.ComunicacionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Pruebas de la persistencia del PDF adjunto.
 *
 * Usan archivos temporales y una base Room en memoria:
 * no tocan los datos reales de la aplicación.
 */
@RunWith(AndroidJUnit4::class)
class AdjuntoPdfTest {

    private val context =
        InstrumentationRegistry.getInstrumentation().targetContext

    private val rutasCreadas = mutableListOf<String>()

    @After
    fun limpiar() {
        rutasCreadas.forEach { AdjuntoPdf.eliminar(context, it) }
    }

    private fun pdfTemporal(bytes: Int): File {
        return File(context.cacheDir, "origen_prueba.pdf").apply {
            writeBytes(ByteArray(bytes) { 7 })
        }
    }

    @Test
    fun copiaElPdfAlAlmacenamientoInternoYSobreviveAlOriginal() = runBlocking {

        val origen = pdfTemporal(2048)

        val resultado = AdjuntoPdf.copiarAInterno(
            context,
            Uri.fromFile(origen),
            "TEST-PDF-1"
        )

        assertTrue(resultado is AdjuntoPdf.ResultadoCopia.Ok)

        val ruta = (resultado as AdjuntoPdf.ResultadoCopia.Ok).ruta
        rutasCreadas += ruta

        // El original desaparece (el Uri del selector era temporal).
        origen.delete()

        val copia = AdjuntoPdf.archivo(context, ruta)

        assertNotNull(copia)
        assertTrue(copia!!.exists())
        assertEquals(2048L, copia.length())
    }

    @Test
    fun origenInexistenteDevuelveErrorYNoDejaArchivos() = runBlocking {

        val resultado = AdjuntoPdf.copiarAInterno(
            context,
            Uri.fromFile(File(context.cacheDir, "no_existe.pdf")),
            "TEST-PDF-2"
        )

        assertTrue(resultado is AdjuntoPdf.ResultadoCopia.Error)

        val huerfano = AdjuntoPdf.archivo(context, "adjuntos/TEST-PDF-2.pdf")

        assertFalse(huerfano?.exists() ?: false)
    }

    @Test
    fun idInvalidoSeRechazaSinEscribirFueraDeAdjuntos() = runBlocking {

        val origen = pdfTemporal(10)

        val resultado = AdjuntoPdf.copiarAInterno(
            context,
            Uri.fromFile(origen),
            "../escape"
        )

        assertTrue(resultado is AdjuntoPdf.ResultadoCopia.Error)
    }

    @Test
    fun rutaFueraDeLaCarpetaDeAdjuntosNoSeResuelve() {

        assertNull(AdjuntoPdf.archivo(context, "../databases/mpaz_notifica_db"))
        assertNull(AdjuntoPdf.archivo(context, "adjuntos/../secreto.txt"))
    }

    @Test
    fun abrirUnaRutaSinArchivoInformaQueNoEstaDisponible() {

        val resultado = AdjuntoPdf.abrir(context, "adjuntos/NO-EXISTE.pdf")

        assertEquals(
            AdjuntoPdf.ResultadoApertura.ARCHIVO_NO_DISPONIBLE,
            resultado
        )
    }

    @Test
    fun roomGuardaYDevuelveLaRutaDelPdf() = runBlocking {

        val base = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        try {

            base.comunicacionDao().insertar(
                ComunicacionEntity(
                    id = "COM-PDF",
                    asunto = "Circular",
                    cuerpo = "Comunicado ficticio de prueba",
                    tipo = "INSTITUCIONAL",
                    alcance = "COLEGIO_COMPLETO",
                    destinoReferencia = null,
                    adjuntoPdfNombre = "circular.pdf",
                    adjuntoPdfRuta = "adjuntos/COM-PDF.pdf",
                    requiereFirmaFisica = false,
                    autorId = "U1",
                    autorNombre = "Inspectoría (prueba)",
                    fechaHora = "2026-11-14 10:00"
                )
            )

            val guardado = base.comunicacionDao().obtenerPorId("COM-PDF")

            assertEquals("circular.pdf", guardado?.adjuntoPdfNombre)
            assertEquals("adjuntos/COM-PDF.pdf", guardado?.adjuntoPdfRuta)

        } finally {
            base.close()
        }
    }
}
