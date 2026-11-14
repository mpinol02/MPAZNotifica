package com.mpaznotifica.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Persistencia de los PDF adjuntos de los comunicados.
 *
 * El selector nativo (OpenDocument) entrega un Uri con permiso temporal:
 * deja de funcionar cuando se cierra la app, por eso no sirve guardarlo.
 * Al enviar el comunicado se COPIA el PDF al almacenamiento interno de la
 * app (filesDir/adjuntos/<idComunicado>.pdf) y en Room se guarda la ruta
 * relativa, de modo que el archivo se pueda volver a abrir desde el detalle.
 *
 * Los archivos son privados de la app; para abrirlos con un visor externo
 * se comparten mediante FileProvider (ver AndroidManifest.xml y
 * res/xml/file_paths.xml).
 */
object AdjuntoPdf {

    private const val TAG = "MPAZ_PDF"

    private const val CARPETA = "adjuntos"

    /** Tamaño máximo aceptado para un adjunto: 10 MB. */
    const val TAMANO_MAXIMO_BYTES: Long = 10L * 1024 * 1024

    private val ID_VALIDO = Regex("[A-Za-z0-9_-]+")

    sealed interface ResultadoCopia {
        /** Copia exitosa. [ruta] es relativa a filesDir. */
        data class Ok(val ruta: String) : ResultadoCopia

        /** El archivo supera [TAMANO_MAXIMO_BYTES]. */
        object DemasiadoGrande : ResultadoCopia

        /** No se pudo leer el documento o escribir la copia. */
        object Error : ResultadoCopia
    }

    enum class ResultadoApertura {
        ABIERTO,
        ARCHIVO_NO_DISPONIBLE,
        SIN_VISOR
    }

    /**
     * Nombre legible del documento elegido (por ejemplo "circular.pdf").
     * Si el proveedor no lo informa, usa el último segmento del Uri.
     */
    fun nombreVisible(context: Context, uri: Uri): String {

        val nombre = try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        } catch (e: Exception) {
            null
        }

        return nombre?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment
            ?: "documento.pdf"
    }

    /**
     * Copia el PDF al almacenamiento interno.
     * Se ejecuta en Dispatchers.IO; si algo falla no deja archivos a medias.
     */
    suspend fun copiarAInterno(
        context: Context,
        uri: Uri,
        comunicadoId: String
    ): ResultadoCopia = withContext(Dispatchers.IO) {

        if (!ID_VALIDO.matches(comunicadoId)) {
            return@withContext ResultadoCopia.Error
        }

        val rutaRelativa = "$CARPETA/$comunicadoId.pdf"

        val destino = archivo(context, rutaRelativa)
            ?: return@withContext ResultadoCopia.Error

        try {

            destino.parentFile?.mkdirs()

            val entrada = context.contentResolver.openInputStream(uri)
                ?: return@withContext ResultadoCopia.Error

            var total = 0L

            entrada.use { input ->
                destino.outputStream().use { salida ->

                    val buffer = ByteArray(8 * 1024)

                    while (true) {

                        val leidos = input.read(buffer)

                        if (leidos < 0) break

                        total += leidos

                        if (total > TAMANO_MAXIMO_BYTES) {
                            throw TamanoExcedidoException()
                        }

                        salida.write(buffer, 0, leidos)
                    }
                }
            }

            ResultadoCopia.Ok(rutaRelativa)

        } catch (e: CancellationException) {

            destino.delete()
            throw e

        } catch (e: TamanoExcedidoException) {

            destino.delete()
            ResultadoCopia.DemasiadoGrande

        } catch (e: Exception) {

            Log.e(TAG, "No se pudo copiar el PDF", e)
            destino.delete()
            ResultadoCopia.Error
        }
    }

    /**
     * Resuelve una ruta relativa guardada en Room.
     * Devuelve null si apunta fuera de la carpeta de adjuntos.
     */
    fun archivo(context: Context, ruta: String): File? {

        return try {

            val base = File(context.filesDir, CARPETA).canonicalFile
            val archivo = File(context.filesDir, ruta).canonicalFile

            if (archivo.path.startsWith(base.path + File.separator)) {
                archivo
            } else {
                null
            }

        } catch (e: IOException) {
            null
        }
    }

    /** Borra la copia (por ejemplo, si no se pudo guardar el comunicado). */
    fun eliminar(context: Context, ruta: String) {
        archivo(context, ruta)?.delete()
    }

    /** Abre el PDF con el visor que tenga el dispositivo. */
    fun abrir(context: Context, ruta: String): ResultadoApertura {

        val archivo = archivo(context, ruta)

        if (archivo == null || !archivo.exists()) {
            return ResultadoApertura.ARCHIVO_NO_DISPONIBLE
        }

        return try {

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)

            ResultadoApertura.ABIERTO

        } catch (e: ActivityNotFoundException) {

            ResultadoApertura.SIN_VISOR

        } catch (e: IllegalArgumentException) {

            Log.e(TAG, "FileProvider no pudo compartir el archivo", e)
            ResultadoApertura.ARCHIVO_NO_DISPONIBLE
        }
    }

    private class TamanoExcedidoException : IOException()
}
