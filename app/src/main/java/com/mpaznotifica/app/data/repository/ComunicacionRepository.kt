
package com.mpaznotifica.app.data.repository

import com.mpaznotifica.app.data.local.AppDatabase
import com.mpaznotifica.app.data.local.ComunicacionEntity
import com.mpaznotifica.app.data.local.EnvioEntity
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.Comunicacion
import com.mpaznotifica.app.model.EnvioDestinatario
import com.mpaznotifica.app.model.EstadoEnvio
import com.mpaznotifica.app.model.TipoComunicacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository de comunicaciones de MPAZ Notifica.
 *
 * Conecta el ViewModel con los DAO de Room.
 *
 * RF-08: registrar resultados de envío.
 * RF-09: historial de comunicados.
 * RF-10: filtros por fecha, tipo y emisor.
 * RF-11: almacenamiento de comunicados.
 *
 * Las comunicaciones enviadas son inmutables.
 */
class ComunicacionRepository(
    private val database: AppDatabase
) {

    private val dao = database.comunicacionDao()
    private val envioDao = database.envioDao()

    // Observar todos los comunicados guardados.
    val comunicados: Flow<List<Comunicacion>> =
        dao.obtenerTodos().map { lista ->
            lista.map { it.toModelo() }
        }

    // Guardar un comunicado sin envíos asociados.
    suspend fun insertar(comunicacion: Comunicacion) {
        dao.insertar(comunicacion.toEntity())
    }

    /**
     * Guarda el comunicado y todos los resultados
     * de despacho mediante una transacción Room.
     *
     * Se utiliza para registrar un envío completo,
     * sin almacenar registros incompletos.
     */
    suspend fun guardarComunicadoConEnvios(
        comunicacion: Comunicacion,
        envios: List<EnvioDestinatario>
    ) {
        require(envios.all {
            it.comunicacionId == comunicacion.id
        }) {
            "Los envíos deben pertenecer al comunicado"
        }

        database.guardarComunicadoConEnvios(
            comunicacion = comunicacion.toEntity(),
            envios = envios.map { it.toEntity() }
        )
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

    // Observar resultados de un comunicado.
    fun obtenerEnvios(
        comunicacionId: String
    ): Flow<List<EnvioDestinatario>> {

        return envioDao.obtenerPorComunicacion(
            comunicacionId
        ).map { lista ->
            lista.map { it.toModelo() }
        }
    }

    // Convertir modelo de comunicación a Entity.
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

    // Convertir Entity de comunicación al modelo.
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

    // Convertir resultado de envío a Entity.
    private fun EnvioDestinatario.toEntity(): EnvioEntity {
        return EnvioEntity(
            id = id,
            comunicacionId = comunicacionId,
            apoderadoId = apoderadoId,
            canal = canal.name,
            estado = estado.name
        )
    }

    // Convertir Entity de envío al modelo.
    private fun EnvioEntity.toModelo(): EnvioDestinatario {
        return EnvioDestinatario(
            id = id,
            comunicacionId = comunicacionId,
            apoderadoId = apoderadoId,
            canal = CanalDespacho.valueOf(canal),
            estado = EstadoEnvio.valueOf(estado)
        )
    }
}
