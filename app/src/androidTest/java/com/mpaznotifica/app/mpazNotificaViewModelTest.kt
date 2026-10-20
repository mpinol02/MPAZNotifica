
package com.mpaznotifica.app

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.viewmodel.MpazNotificaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas automatizadas del equipo MPAZ Notifica.
 *
 * Verifica reglas de autenticación, perfiles
 * y validaciones de comunicados.
 *
 * Estas pruebas no envían comunicados ni
 * modifican los registros guardados.
 */
@RunWith(AndroidJUnit4::class)
class mpazNotificaViewModelTest {

    private lateinit var viewModel: MpazNotificaViewModel

    @Before
    fun preparar() {
        val application =
            InstrumentationRegistry.getInstrumentation()
                .targetContext.applicationContext as Application

        viewModel = MpazNotificaViewModel(application)
    }

    /**
     * E01: el Profesor Jefe solamente
     * puede utilizar su curso asignado.
     */
    @Test
    fun profesorNoPuedeEnviarAOtroCurso() {

        val ingreso = viewModel.login(
            "profesor.4basicoA@mpaznotifica.cl",
            "profesor123"
        )

        assertTrue(ingreso)

        val errores = viewModel.validarComunicado(
            asunto = "Comunicado de prueba",
            cuerpo = "Mensaje ficticio",
            tipo = TipoComunicacion.PEDAGOGICA,
            alcance = AlcanceEnvio.CURSO,
            destino = "2° Básico A"
        )

        assertTrue(errores.hayErrores)
        assertTrue(errores.destino != null)
    }

    /**
     * E02: no se permite validar un
     * comunicado sin iniciar sesión.
     */
    @Test
    fun noPermitirComunicadoSinSesion() {

        val errores = viewModel.validarComunicado(
            asunto = "Comunicado de prueba",
            cuerpo = "Mensaje ficticio",
            tipo = TipoComunicacion.PEDAGOGICA,
            alcance = AlcanceEnvio.CURSO,
            destino = "4° Básico A"
        )

        assertTrue(errores.hayErrores)
    }

    /**
     * E03: el formulario no acepta
     * asunto ni mensaje vacíos.
     */
    @Test
    fun formularioRechazaCamposVacios() {

        viewModel.login(
            "inspectoria@mpaznotifica.cl",
            "inspectoria123"
        )

        val errores = viewModel.validarComunicado(
            asunto = "",
            cuerpo = "",
            tipo = null,
            alcance = null,
            destino = null
        )

        assertTrue(errores.hayErrores)
        assertTrue(errores.asunto != null)
        assertTrue(errores.cuerpo != null)
        assertTrue(errores.tipo != null)
        assertTrue(errores.alcance != null)
    }

    /**
     * Comprueba que Inspectoría General
     * dispone de los cuatro alcances.
     */
    @Test
    fun inspectoriaTieneCuatroAlcances() {

        viewModel.login(
            "inspectoria@mpaznotifica.cl",
            "inspectoria123"
        )

        val usuario = viewModel.usuarioActivo.value

        val alcances =
            viewModel.alcancesPermitidos(usuario)

        assertEquals(4, alcances.size)
        assertTrue(
            AlcanceEnvio.COLEGIO_COMPLETO in alcances
        )
    }

    /**
     * Comprueba que un Profesor Jefe
     * puede validar su propio curso.
     */
    @Test
    fun profesorPuedeUsarSuCurso() {

        viewModel.login(
            "profesor.4basicoA@mpaznotifica.cl",
            "profesor123"
        )

        val errores = viewModel.validarComunicado(
            asunto = "Reunión de curso",
            cuerpo = "Comunicado ficticio de prueba",
            tipo = TipoComunicacion.CITACION,
            alcance = AlcanceEnvio.CURSO,
            destino = "4° Básico A"
        )

        assertFalse(errores.hayErrores)
    }
}
