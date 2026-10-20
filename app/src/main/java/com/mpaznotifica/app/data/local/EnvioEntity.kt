
package com.mpaznotifica.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro de despacho simulado de un comunicado
 * hacia un apoderado ficticio.
 *
 * RF-08: registrar estado del envío por destinatario.
 * RF-11: conservar canal y destinatario.
 *
 * Cada envío pertenece a un comunicado.
 */
@Entity(
    tableName = "envios",
    foreignKeys = [
        ForeignKey(
            entity = ComunicacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["comunicacionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["comunicacionId"])]
)
data class EnvioEntity(
    @PrimaryKey
    val id: String,

    val comunicacionId: String,

    val apoderadoId: String,

    val canal: String,

    val estado: String
)
