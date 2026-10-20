
package com.mpaznotifica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar los comunicados.
 *
 * RF-09: mantener un historial de comunicados.
 * RF-11: registrar autor, fecha y contenido.
 * RN-02: los comunicados enviados son inmutables.
 *
 * Todos los datos utilizados son ficticios.
 */
@Entity(tableName = "comunicaciones")
data class ComunicacionEntity(

    @PrimaryKey
    val id: String,

    val asunto: String,

    val cuerpo: String,

    val tipo: String,

    val alcance: String,

    val destinoReferencia: String?,

    val adjuntoPdfNombre: String?,

    val requiereFirmaFisica: Boolean,

    val autorId: String,

    val autorNombre: String,

    val fechaHora: String
)
