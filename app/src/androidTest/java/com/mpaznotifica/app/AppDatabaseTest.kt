
package com.mpaznotifica.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry

import com.mpaznotifica.app.data.local.AppDatabase
import com.mpaznotifica.app.data.local.ApoderadoEntity
import com.mpaznotifica.app.data.local.ComunicacionEntity
import com.mpaznotifica.app.data.local.EnvioEntity

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas automatizadas de persistencia Room.
 *
 * Proyecto: MPAZ Notifica
 *
 * Se utiliza una base de datos temporal en memoria.
 * No se modifica la base de datos real de la aplicación.
 *
 * Se comprueba:
 * 1. Guardar y recuperar comunicados.
 * 2. Registrar envíos por destinatario.
 * 3. Actualizar el canal preferente.
 * 4. Conservar las preferencias existentes.
 * 5. Mantener la relación entre comunicados y envíos.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun prepararBaseDeDatos() {

        val context =
            InstrumentationRegistry.getInstrumentation()
                .targetContext

        // Crear base de datos temporal para cada prueba.
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun cerrarBaseDeDatos() {

        // Eliminar los datos temporales al terminar.
        database.close()
    }

    /**
     * PRUEBA 1:
     * Guardar y recuperar un comunicado desde Room.
     */
    @Test
    fun guardarYRecuperarComunicado() = runBlocking {

        val comunicado = crearComunicado(
            id = "COM-001"
        )

        database.comunicacionDao().insertar(
            comunicado
        )

        val resultado =
            database.comunicacionDao().obtenerPorId(
                "COM-001"
            )

        assertNotNull(resultado)
        assertEquals(
            "Reunión de apoderados",
            resultado?.asunto
        )

        assertEquals(
            "PROF-01",
            resultado?.autorId
        )
    }

    /**
     * PRUEBA 2:
     * Guardar un comunicado con sus resultados
     * de despacho simulado.
     */
    @Test
    fun guardarComunicadoConEnvios() = runBlocking {

        val comunicado = crearComunicado(
            id = "COM-002"
        )

        val envios = listOf(
            crearEnvio(
                id = "ENV-001",
                comunicacionId = "COM-002",
                apoderadoId = "APO-001",
                canal = "EMAIL"
            ),
            crearEnvio(
                id = "ENV-002",
                comunicacionId = "COM-002",
                apoderadoId = "APO-002",
                canal = "WHATSAPP"
            )
        )

        database.guardarComunicadoConEnvios(
            comunicacion = comunicado,
            envios = envios
        )

        val resultado =
            database.envioDao()
                .obtenerPorComunicacion("COM-002")
                .first()

        assertEquals(2, resultado.size)

        assertTrue(
            resultado.any {
                it.apoderadoId == "APO-001" &&
                        it.canal == "EMAIL"
            }
        )

        assertTrue(
            resultado.any {
                it.apoderadoId == "APO-002" &&
                        it.canal == "WHATSAPP"
            }
        )
    }

    /**
     * PRUEBA 3:
     * Actualizar el canal preferente del apoderado.
     */
    @Test
    fun actualizarCanalPreferente() = runBlocking {

        val apoderado = crearApoderado(
            id = "APO-001",
            canal = "EMAIL"
        )

        database.apoderadoDao().insertarTodos(
            listOf(apoderado)
        )

        val filasActualizadas =
            database.apoderadoDao().actualizarCanal(
                apoderadoId = "APO-001",
                canal = "SMS"
            )

        assertEquals(1, filasActualizadas)

        val resultado =
            database.apoderadoDao()
                .obtenerTodos()
                .first()

        assertEquals(1, resultado.size)
        assertEquals(
            "SMS",
            resultado.first().canalPreferente
        )
    }

    /**
     * PRUEBA 4:
     * La inicialización no debe sobrescribir
     * un canal preferente ya configurado.
     */
    @Test
    fun inicializacionNoSobrescribePreferencias() =
        runBlocking {

            val apoderadoOriginal = crearApoderado(
                id = "APO-001",
                canal = "EMAIL"
            )

            database.apoderadoDao().insertarTodos(
                listOf(apoderadoOriginal)
            )

            database.apoderadoDao().actualizarCanal(
                apoderadoId = "APO-001",
                canal = "WHATSAPP"
            )

            // Simular una nueva inicialización.
            database.apoderadoDao().insertarTodos(
                listOf(apoderadoOriginal)
            )

            val resultado =
                database.apoderadoDao()
                    .obtenerTodos()
                    .first()

            assertEquals(1, resultado.size)

            // Debe conservar la preferencia actualizada.
            assertEquals(
                "WHATSAPP",
                resultado.first().canalPreferente
            )
        }

    /**
     * PRUEBA 5:
     * No debe registrarse un envío asociado
     * a un comunicado inexistente.
     *
     * Se comprueba la restricción de clave foránea.
     */
    @Test
    fun envioRequiereComunicadoExistente() =
        runBlocking {

            val envioInvalido = crearEnvio(
                id = "ENV-INVALIDO",
                comunicacionId = "NO-EXISTE",
                apoderadoId = "APO-001",
                canal = "EMAIL"
            )

            var errorDetectado = false

            try {
                database.envioDao().insertarTodos(
                    listOf(envioInvalido)
                )
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                errorDetectado = true
            } catch (e: android.database.sqlite.SQLiteException) {
                errorDetectado = true
            }

            assertTrue(
                "Room debe impedir envíos sin comunicado",
                errorDetectado
            )

            val resultado =
                database.envioDao()
                    .obtenerPorComunicacion("NO-EXISTE")
                    .first()

            assertTrue(resultado.isEmpty())

            val comunicado =
                database.comunicacionDao()
                    .obtenerPorId("NO-EXISTE")

            assertNull(comunicado)
        }

    // ============================================================
    // Datos ficticios utilizados únicamente por estas pruebas.
    // ============================================================

    private fun crearComunicado(
        id: String
    ): ComunicacionEntity {

        return ComunicacionEntity(
            id = id,
            asunto = "Reunión de apoderados",
            cuerpo = "Comunicado ficticio de prueba",
            tipo = "PEDAGOGICA",
            alcance = "CURSO",
            destinoReferencia = "4° Básico A",
            adjuntoPdfNombre = null,
            requiereFirmaFisica = false,
            autorId = "PROF-01",
            autorNombre = "Profesor Jefe de prueba",
            fechaHora = "2026-10-20 12:00"
        )
    }

    private fun crearEnvio(
        id: String,
        comunicacionId: String,
        apoderadoId: String,
        canal: String
    ): EnvioEntity {

        return EnvioEntity(
            id = id,
            comunicacionId = comunicacionId,
            apoderadoId = apoderadoId,
            canal = canal,
            estado = "DESPACHADO"
        )
    }

    private fun crearApoderado(
        id: String,
        canal: String
    ): ApoderadoEntity {

        return ApoderadoEntity(
            id = id,
            nombreCompleto = "Apoderado de prueba",
            correo = "prueba@ejemplo.cl",
            telefono = "+56900000000",
            canalPreferente = canal,
            pupilosIds = "[\"EST-001\"]"
        )
    }
}
