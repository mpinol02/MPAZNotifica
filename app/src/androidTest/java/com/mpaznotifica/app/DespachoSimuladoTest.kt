
package com.mpaznotifica.app.viewmodel

import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.EstadoEnvio
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias del despacho simulado.
 *
 * Verifican DESPACHADO y FALLIDO
 * para EMAIL, SMS y WHATSAPP.
 */
class DespachoSimuladoTest {

    @Test
    fun emailConCorreoEsDespachado() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.EMAIL,
            correo = "apoderado@ejemplo.cl",
            telefono = ""
        )

        assertEquals(
            EstadoEnvio.DESPACHADO,
            resultado
        )
    }

    @Test
    fun emailSinCorreoEsFallido() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.EMAIL,
            correo = "",
            telefono = "912345678"
        )

        assertEquals(
            EstadoEnvio.FALLIDO,
            resultado
        )
    }

    @Test
    fun smsConTelefonoEsDespachado() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.SMS,
            correo = "",
            telefono = "912345678"
        )

        assertEquals(
            EstadoEnvio.DESPACHADO,
            resultado
        )
    }

    @Test
    fun smsSinTelefonoEsFallido() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.SMS,
            correo = "apoderado@ejemplo.cl",
            telefono = ""
        )

        assertEquals(
            EstadoEnvio.FALLIDO,
            resultado
        )
    }

    @Test
    fun whatsappConTelefonoEsDespachado() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.WHATSAPP,
            correo = "",
            telefono = "912345678"
        )

        assertEquals(
            EstadoEnvio.DESPACHADO,
            resultado
        )
    }

    @Test
    fun whatsappSinTelefonoEsFallido() {

        val resultado = DespachoSimulado.estadoPara(
            canal = CanalDespacho.WHATSAPP,
            correo = "apoderado@ejemplo.cl",
            telefono = ""
        )

        assertEquals(
            EstadoEnvio.FALLIDO,
            resultado
        )
    }
}
