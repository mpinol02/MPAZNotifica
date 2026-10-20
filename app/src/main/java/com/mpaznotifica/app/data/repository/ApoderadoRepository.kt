
package com.mpaznotifica.app.data.repository

import com.mpaznotifica.app.data.local.ApoderadoDao
import com.mpaznotifica.app.data.local.ApoderadoEntity
import com.mpaznotifica.app.model.Apoderado
import com.mpaznotifica.app.model.CanalDespacho
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

/**
 * Repository de apoderados ficticios de MPAZ Notifica.
 *
 * Permite almacenar y consultar el canal preferente
 * utilizando Room.
 *
 * Los apoderados no operan directamente la aplicación.
 */
class ApoderadoRepository(
    private val dao: ApoderadoDao
) {

    // Observar apoderados desde Room.
    val apoderados: Flow<List<Apoderado>> =
        dao.obtenerTodos().map { lista ->
            lista.map { it.toModelo() }
        }

    // Registrar los apoderados de ejemplo.
    // Los existentes no se sobrescriben.
    suspend fun inicializar(
        apoderadosIniciales: List<Apoderado>
    ) {
        dao.insertarTodos(
            apoderadosIniciales.map { it.toEntity() }
        )
    }

    // Actualizar el canal preferente.
    suspend fun actualizarCanal(
        apoderadoId: String,
        canal: CanalDespacho
    ): Boolean {
        return dao.actualizarCanal(
            apoderadoId,
            canal.name
        ) > 0
    }

    // Transformar modelo a entidad Room.
    private fun Apoderado.toEntity(): ApoderadoEntity {
        return ApoderadoEntity(
            id = id,
            nombreCompleto = nombreCompleto,
            correo = correo,
            telefono = telefono,
            canalPreferente = canalPreferente.name,
            pupilosIds = JSONArray(pupilosIds).toString()
        )
    }

    // Transformar entidad Room a modelo.
    private fun ApoderadoEntity.toModelo(): Apoderado {

        val json = JSONArray(pupilosIds)

        val ids = (0 until json.length()).map {
            json.getString(it)
        }

        return Apoderado(
            id = id,
            nombreCompleto = nombreCompleto,
            correo = correo,
            telefono = telefono,
            canalPreferente = CanalDespacho.valueOf(
                canalPreferente
            ),
            pupilosIds = ids
        )
    }
}
