
package com.mpaznotifica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO para administrar los comunicados almacenados en Room.
 *
 * RF-09: mantener el historial de comunicados.
 * RF-10: filtrar por fecha, tipo y emisor.
 * RF-11: registrar y consultar comunicados.
 *
 * RN-02: los comunicados enviados son inmutables.
 * Por eso no existen operaciones Update ni Delete.
 */
@Dao
interface ComunicacionDao {

    // Guardar un nuevo comunicado.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(
        comunicacion: ComunicacionEntity
    )

    // Obtener todos los comunicados.
    @Query(
        "SELECT * FROM comunicaciones ORDER BY fechaHora DESC"
    )
    fun obtenerTodos(): Flow<List<ComunicacionEntity>>

    // Consultar un comunicado específico.
    @Query(
        "SELECT * FROM comunicaciones WHERE id = :id LIMIT 1"
    )
    suspend fun obtenerPorId(
        id: String
    ): ComunicacionEntity?

    // Filtrar historial por tipo, emisor y rango de fechas.
    @Query(
        """
        SELECT * FROM comunicaciones
        WHERE (:tipo IS NULL OR tipo = :tipo)
        AND (:emisorId IS NULL OR autorId = :emisorId)
        AND (:fechaDesde IS NULL OR substr(fechaHora, 1, 10) >= :fechaDesde)
        AND (:fechaHasta IS NULL OR substr(fechaHora, 1, 10) <= :fechaHasta)
        ORDER BY fechaHora DESC
        """
    )
    fun obtenerFiltrados(
        tipo: String?,
        emisorId: String?,
        fechaDesde: String?,
        fechaHasta: String?
    ): Flow<List<ComunicacionEntity>>
}
