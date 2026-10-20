
package com.mpaznotifica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO de los envíos simulados.
 *
 * Permite guardar los resultados y consultar
 * los destinatarios de cada comunicado.
 *
 * Se conserva la trazabilidad sin modificar
 * los registros después de su creación.
 */
@Dao
interface EnvioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarTodos(envios: List<EnvioEntity>)

    @Query(
        "SELECT * FROM envios WHERE comunicacionId = :comunicacionId"
    )
    fun obtenerPorComunicacion(
        comunicacionId: String
    ): Flow<List<EnvioEntity>>
}
