
package com.mpaznotifica.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar los datos ficticios
 * de los apoderados y su canal preferente.
 *
 * Los apoderados son destinatarios y no utilizan
 * directamente la aplicación.
 *
 * Se conservan los identificadores de los estudiantes
 * asociados mediante una lista serializada.
 */
@Entity(tableName = "apoderados")
data class ApoderadoEntity(

    @PrimaryKey
    val id: String,

    val nombreCompleto: String,

    val correo: String,

    val telefono: String,

    val canalPreferente: String,

    val pupilosIds: String
)
