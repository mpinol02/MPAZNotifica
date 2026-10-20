
package com.mpaznotifica.app.data.repository

import com.mpaznotifica.app.data.local.ComunicacionDao
import com.mpaznotifica.app.data.local.ComunicacionEntity
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.Comunicacion
import com.mpaznotifica.app.model.TipoComunicacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository de comunicaciones de MPAZ Notifica.
 *
 * Conecta el ViewModel con el DAO de Room.
 *
 * RF-09: historial de comunicados.
 * RF-10: filtros por fecha, tipo y emisor.
 * RF-11: almacenamiento de comunicados.
 *
 * Las comunicaciones enviadas son inmutables.
 */
class ComunicacionRepository(
    private val dao: ComunicacionDao
) {

    // Observar todos los comunicados guardados.
    val comunicados: Flow<List<Comunicacion>> =
        dao.obtenerTodos().map { lista ->
            lista.map { it.toModelo() }
        }

    // Guardar un comunicado.
    suspend fun insertar(comunicacion: Comunicacion) {
        dao.insertar(comunicacion.toEntity())
    }

    // Buscar un comunicado por su identificador.
    suspend fun obtenerPorId(id: String): Comunicacion? {
        return dao.obtenerPorId(id)?.toModelo()
    }

    // Consultar historial con filtros.
    fun obtenerFiltrados(
        tipo: TipoComunicacion?,
        emisorId: String?,
        fechaDesde: String?,
        fechaHasta: String?
    ): Flow<List<Comunicacion>> {

        return dao.obtenerFiltrados(
            tipo = tipo?.name,
            emisorId = emisorId,
            fechaDesde = fechaDesde,
            fechaHasta = fechaHasta
        ).map { lista ->
            lista.map { it.toModelo() }
        }
    }

    // Convertir el modelo de la aplicación a Entity de Room.
    private fun Comunicacion.toEntity(): ComunicacionEntity {
        return ComunicacionEntity(
            id = id,
            asunto = asunto,
            cuerpo = cuerpo,
            tipo = tipo.name,
            alcance = alcance.name,
            destinoReferencia = destinoReferencia,
            adjuntoPdfNombre = adjuntoPdfNombre,
            requiereFirmaFisica = requiereFirmaFisica,
            autorId = autorId,
            autorNombre = autorNombre,
            fechaHora = fechaHora
        )
    }

    // Convertir Entity de Room al modelo de la aplicación.
    private fun ComunicacionEntity.toModelo(): Comunicacion {
        return Comunicacion(
            id = id,
            asunto = asunto,
            cuerpo = cuerpo,
            tipo = TipoComunicacion.valueOf(tipo),
            alcance = AlcanceEnvio.valueOf(alcance),
            destinoReferencia = destinoReferencia,
            adjuntoPdfNombre = adjuntoPdfNombre,
            requiereFirmaFisica = requiereFirmaFisica,
            autorId = autorId,
            autorNombre = autorNombre,
            fechaHora = fechaHora
        )
    }
}
