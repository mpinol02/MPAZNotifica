
package com.mpaznotifica.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO para administrar los apoderados ficticios
 * y sus canales preferentes.
 *
 * Permite consultar apoderados y modificar únicamente
 * el canal preferente cuando corresponda.
 */
@Dao
interface ApoderadoDao {

    // Obtener los apoderados almacenados.
    @Query("SELECT * FROM apoderados ORDER BY nombreCompleto")
    fun obtenerTodos(): Flow<List<ApoderadoEntity>>

    // Insertar apoderados ficticios inicialmente.
    // IGNORE evita sobrescribir preferencias existentes.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(
        apoderados: List<ApoderadoEntity>
    )

    // Actualizar solamente el canal preferente.
    @Query(
        """
        UPDATE apoderados
        SET canalPreferente = :canal
        WHERE id = :apoderadoId
        """
    )
    suspend fun actualizarCanal(
        apoderadoId: String,
        canal: String
    ): Int
}
