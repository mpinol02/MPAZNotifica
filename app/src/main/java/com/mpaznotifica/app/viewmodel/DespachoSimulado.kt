
package com.mpaznotifica.app.viewmodel

import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.EstadoEnvio

/**
 * Simula el resultado de un despacho.
 *
 * No envía mensajes reales.
 *
 * EMAIL requiere correo.
 * SMS y WHATSAPP requieren teléfono.
 *
 * Si falta el contacto correspondiente,
 * devuelve FALLIDO.
 */
object DespachoSimulado {

    fun estadoPara(
        canal: CanalDespacho,
        correo: String,
        telefono: String
    ): EstadoEnvio {

        val tieneContacto = when (canal) {

            CanalDespacho.EMAIL -> {
                correo.isNotBlank()
            }

            CanalDespacho.SMS,
            CanalDespacho.WHATSAPP -> {
                telefono.isNotBlank()
            }
        }

        return if (tieneContacto) {
            EstadoEnvio.DESPACHADO
        } else {
            EstadoEnvio.FALLIDO
        }
    }
}
