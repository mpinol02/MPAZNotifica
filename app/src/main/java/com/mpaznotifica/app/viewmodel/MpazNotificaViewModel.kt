
package com.mpaznotifica.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mpaznotifica.app.data.MockData
import com.mpaznotifica.app.data.local.AppDatabase
import com.mpaznotifica.app.data.repository.ApoderadoRepository
import com.mpaznotifica.app.data.repository.ComunicacionRepository
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.Apoderado
import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.Comunicacion
import com.mpaznotifica.app.model.EnvioDestinatario
import com.mpaznotifica.app.model.ErroresComunicado
import com.mpaznotifica.app.model.EstadoEnvio
import com.mpaznotifica.app.model.Perfil
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_DSY1105-007 al que corresponde. Mientras no se implemente:
 *  - el login siempre rechaza (usar "Modo desarrollo" en la pantalla de login),
 *  - cualquier perfil ve todos los alcances y tipos (sin restricción por perfil),
 *  - enviar un comunicado o cambiar el canal de un apoderado no hace nada,
 *  - los filtros del historial no filtran.
 *
 * Actualización del equipo:
 * La lógica de negocio principal ya fue implementada.
 * Se incorporan las correcciones RF-06 solicitadas por el docente.
 * Los comentarios originales se conservan como referencia.
 */
class MpazNotificaViewModel(
    application: Application
) : AndroidViewModel(application) {

    // Integración del equipo: acceso a Room mediante Repository.
    private val database = AppDatabase.obtenerInstancia(application)

    private val comunicacionRepository =
        ComunicacionRepository(database)

    // Integración del equipo: persistencia de apoderados en Room.
    private val apoderadoRepository =
        ApoderadoRepository(database.apoderadoDao())

    // =====================================================================
    // Módulo "Autenticación y perfiles"
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)

    val usuarioActivo: StateFlow<Usuario?> =
        _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)

    val errorLogin: StateFlow<String?> =
        _errorLogin.asStateFlow()

    /**
     * TODO(equipo): inicio de sesión diferenciado para Inspectoría General y Profesor/a Jefe.
     * Validar campos vacíos (patrón Guía 11) y luego comparar contra MockData.usuarios (o el
     * backend que confirme el CITT). Si es correcto: dejar _usuarioActivo y devolver true.
     * Si no: dejar _errorLogin con un mensaje y devolver false.
     *
     * Implementado por el equipo mediante credenciales ficticias.
     */
    fun login(
        usuario: String,
        clave: String
    ): Boolean {

        val correo = usuario.trim()

        if (correo.isBlank()) {
            _usuarioActivo.value = null
            _errorLogin.value = "Debe ingresar su correo institucional"
            return false
        }

        if (clave.isBlank()) {
            _usuarioActivo.value = null
            _errorLogin.value = "Debe ingresar su contraseña"
            return false
        }

        val usuarioEncontrado = MockData.usuarios.find {
            it.usuario.equals(correo, ignoreCase = true) &&
                    it.clave == clave
        }

        if (usuarioEncontrado == null) {
            _usuarioActivo.value = null
            _errorLogin.value = "Correo o contraseña incorrectos"
            return false
        }

        _usuarioActivo.value = usuarioEncontrado
        _errorLogin.value = null

        return true
    }

    /**
     * SOLO PARA DESARROLLO: entra con el usuario de prueba del perfil indicado, sin validar
     * credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     *
     * Pendiente de retirar en la etapa de limpieza final.
     * Los botones de desarrollo ya fueron retirados del LoginScreen.
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first {
            it.perfil == perfil
        }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _comunicadoSeleccionadoId.value = null
    }

    // =====================================================================
    // Datos de referencia (no es lógica de negocio)
    // =====================================================================

    val cursos: List<String> = MockData.cursos

    val talleres = MockData.talleres

    val emisores: List<Usuario> = MockData.usuarios

    private val _apoderados =
        MutableStateFlow(MockData.apoderados)

    val apoderados: StateFlow<List<Apoderado>> =
        _apoderados.asStateFlow()

    /**
     * Integración Room:
     * cargar los apoderados ficticios inicialmente
     * y observar los cambios desde SQLite.
     *
     * Los registros existentes no se sobrescriben,
     * conservando el canal preferente elegido.
     */
    init {
        viewModelScope.launch {
            try {
                apoderadoRepository.inicializar(
                    MockData.apoderados
                )

                apoderadoRepository.apoderados.collect { lista ->
                    _apoderados.value = lista
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "MPAZ_ROOM",
                    "Error al cargar los apoderados",
                    e
                )
            }
        }
    }

    fun apoderadoPorId(id: String): Apoderado? =
        _apoderados.value.find {
            it.id == id
        }

    fun nombresPupilos(
        apoderado: Apoderado
    ): List<String> =
        MockData.estudiantes
            .filter {
                it.id in apoderado.pupilosIds
            }
            .map {
                "${it.nombreReferencial} (${it.curso})"
            }

    // =====================================================================
    // Módulo "Redacción y envío"
    // =====================================================================

    /**
     * TODO(equipo): devolver solo los alcances que el perfil puede usar.
     * RF: "restringir las opciones de emisión según el perfil autenticado".
     * Regla de negocio: un Profesor/a Jefe no puede emitir a "colegio completo" ni a un curso
     * distinto al propio (alcance sobre talleres pendiente de confirmar con el CITT).
     * Hoy devuelve los cuatro alcances para cualquier perfil.
     *
     * RF-06 actualizado:
     * Inspectoría General puede utilizar todos los alcances.
     * Profesor Jefe puede enviar a su curso o a apoderados
     * individuales relacionados con su curso.
     */
    fun alcancesPermitidos(
        usuario: Usuario?
    ): List<AlcanceEnvio> {

        return when (usuario?.perfil) {

            Perfil.INSPECTORIA_GENERAL ->
                AlcanceEnvio.entries.toList()

            Perfil.PROFESOR_JEFE ->
                listOf(
                    AlcanceEnvio.CURSO,
                    AlcanceEnvio.APODERADO_INDIVIDUAL
                )

            else -> emptyList()
        }
    }

    /**
     * TODO(equipo): devolver solo los tipos de comunicación que el perfil puede emitir.
     * Criterio de éxito del caso: un Profesor/a Jefe no puede enviar circulares institucionales,
     * solo lo referente a su curso. Hoy devuelve todos los tipos.
     *
     * Implementación actual:
     * Inspectoría General tiene todos los tipos.
     * Profesor Jefe utiliza los tipos pedagógicos
     * y de citaciones definidos para el proyecto.
     *
     * Los tipos permitidos deben verificarse con
     * las precisiones finales del docente y CITT.
     */
    fun tiposPermitidos(
        usuario: Usuario?
    ): List<TipoComunicacion> {

        return when (usuario?.perfil) {

            Perfil.INSPECTORIA_GENERAL ->
                TipoComunicacion.entries.toList()

            Perfil.PROFESOR_JEFE ->
                listOf(
                    TipoComunicacion.PEDAGOGICA,
                    TipoComunicacion.CITACION,
                    TipoComunicacion.SALIDA_PEDAGOGICA
                )

            else -> emptyList()
        }
    }

    /**
     * TODO(equipo): opciones de destino según el alcance, respetando el perfil (por ejemplo, un
     * Profesor/a Jefe solo debería ver su propio curso). Hoy muestra todas las opciones.
     *
     * RF-06:
     * Para apoderados individuales se cruzan los pupilos
     * del apoderado con los estudiantes del curso asignado.
     */
    fun destinosPara(
        alcance: AlcanceEnvio?,
        usuario: Usuario?
    ): List<String> {

        if (
            usuario == null ||
            alcance !in alcancesPermitidos(usuario)
        ) {
            return emptyList()
        }

        return when (alcance) {

            AlcanceEnvio.CURSO -> {

                if (usuario.perfil == Perfil.PROFESOR_JEFE) {
                    listOfNotNull(
                        usuario.cursoAsignado
                    )
                } else {
                    cursos
                }
            }

            AlcanceEnvio.APODERADO_INDIVIDUAL -> {

                if (usuario.perfil == Perfil.PROFESOR_JEFE) {

                    // Identificadores de estudiantes de su curso.
                    val estudiantesDeSuCurso =
                        MockData.estudiantes
                            .filter {
                                it.curso == usuario.cursoAsignado
                            }
                            .map {
                                it.id
                            }
                            .toSet()

                    // Solo apoderados con al menos un pupilo
                    // perteneciente al curso asignado.
                    _apoderados.value
                        .filter { apoderado ->
                            apoderado.pupilosIds.any {
                                it in estudiantesDeSuCurso
                            }
                        }
                        .map {
                            it.nombreCompleto
                        }

                } else {

                    // Inspectoría puede elegir todos
                    // los apoderados ficticios.
                    _apoderados.value.map {
                        it.nombreCompleto
                    }
                }
            }

            AlcanceEnvio.TALLER ->
                talleres.map {
                    it.nombre
                }

            AlcanceEnvio.COLEGIO_COMPLETO,
            null -> emptyList()
        }
    }

    /**
     * TODO(equipo): validar el formulario y devolver los errores por campo: asunto y cuerpo
     * obligatorios, tipo y alcance elegidos, destino obligatorio salvo "colegio completo", y que
     * el alcance esté permitido para el perfil. Hoy devuelve "sin errores" siempre.
     *
     * Implementado por el equipo.
     * RF-06: comprueba los permisos también en el ViewModel,
     * incluso cuando alguien intenta utilizar un destino
     * no autorizado directamente.
     */
    fun validarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion?,
        alcance: AlcanceEnvio?,
        destino: String?
    ): ErroresComunicado {

        val usuario = _usuarioActivo.value

        val errorAsunto = when {

            asunto.isBlank() ->
                "Debe ingresar el asunto"

            else -> null
        }

        val errorCuerpo = when {

            cuerpo.isBlank() ->
                "Debe ingresar el mensaje"

            else -> null
        }

        val errorTipo = when {

            tipo == null ->
                "Debe seleccionar un tipo de comunicación"

            usuario == null ->
                "Debe iniciar sesión"

            tipo !in tiposPermitidos(usuario) ->
                "Este tipo de comunicación no está permitido"

            else -> null
        }

        val errorAlcance = when {

            alcance == null ->
                "Debe seleccionar un alcance"

            usuario == null ->
                "Debe iniciar sesión"

            alcance !in alcancesPermitidos(usuario) -> {
                if (usuario.perfil == Perfil.PROFESOR_JEFE) {
                    "Solo puedes enviar comunicados a tu curso"
                } else {
                    "No tiene permiso para utilizar este alcance"
                }
            }

            else -> null
        }

        val errorDestino = when {

            alcance == null -> null

            usuario == null ->
                "Debe iniciar sesión"

            alcance !in alcancesPermitidos(usuario) -> {
                if (usuario.perfil == Perfil.PROFESOR_JEFE) {
                    "Solo puedes enviar comunicados a tu curso"
                } else {
                    "No tiene permiso para este destino"
                }
            }

            alcance == AlcanceEnvio.COLEGIO_COMPLETO ->
                null

            destino.isNullOrBlank() ->
                "Debe seleccionar un destinatario"

            destino !in destinosPara(alcance, usuario) -> {
                if (usuario.perfil == Perfil.PROFESOR_JEFE) {
                    "Solo puedes enviar comunicados a tu curso"
                } else {
                    "El destinatario seleccionado no está permitido"
                }
            }

            else -> null
        }

        return ErroresComunicado(
            asunto = errorAsunto,
            cuerpo = errorCuerpo,
            tipo = errorTipo,
            alcance = errorAlcance,
            destino = errorDestino
        )
    }

    // Integración del equipo: historial observado desde Room.
    val comunicados: StateFlow<List<Comunicacion>> =
        comunicacionRepository.comunicados.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Se conserva esta lista del proyecto base por compatibilidad.
    private val _envios =
        MutableStateFlow(MockData.envios)

    /**
     * TODO(equipo): registrar el comunicado y simular su despacho. RF del módulo:
     *  - guardar automáticamente autor (usuarioActivo), fecha/hora, contenido y destinatarios;
     *  - resolver los apoderados destinatarios según el alcance (curso, taller, individual o todos);
     *  - simular el envío por el canal preferente de cada apoderado mediante un servicio mock
     *    (ver data/remote/ApiPendiente.kt) y guardar un EnvioDestinatario con su estado
     *    (despachado/fallido) por cada uno;
     *  - si requiereFirmaFisica: solo informar por correo y alertar que corresponde firma en
     *    papel (no capturar firmas).
     * Regla: el registro queda inmutable una vez enviado.
     * Hoy no hace nada: el comunicado no aparece en el historial.
     *
     * Implementado por el equipo:
     * el comunicado y sus envíos se guardan en Room
     * utilizando una transacción.
     *
     * Pendiente de la siguiente etapa:
     * generar el estado FALLIDO si faltan datos de contacto.
     */
    suspend fun enviarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion,
        alcance: AlcanceEnvio,
        destino: String?,
        adjuntoPdfNombre: String?,
        requiereFirmaFisica: Boolean
    ): Boolean {

        // RF-06: comprobar permisos antes del envío.
        val errores = validarComunicado(
            asunto = asunto,
            cuerpo = cuerpo,
            tipo = tipo,
            alcance = alcance,
            destino = destino
        )

        if (errores.hayErrores) {
            return false
        }

        val usuario =
            _usuarioActivo.value ?: return false

        // Resolver destinatarios utilizando datos ficticios.
        val destinatarios = when (alcance) {

            AlcanceEnvio.APODERADO_INDIVIDUAL -> {

                _apoderados.value.filter {
                    it.nombreCompleto == destino
                }
            }

            AlcanceEnvio.CURSO -> {

                val idsEstudiantes =
                    MockData.estudiantes
                        .filter {
                            it.curso == destino
                        }
                        .map {
                            it.id
                        }
                        .toSet()

                _apoderados.value.filter { apoderado ->
                    apoderado.pupilosIds.any {
                        it in idsEstudiantes
                    }
                }
            }

            AlcanceEnvio.TALLER -> {

                val taller = talleres.find {
                    it.nombre == destino
                } ?: return false

                val idsEstudiantes =
                    MockData.estudiantes
                        .filter {
                            taller.id in it.talleresIds
                        }
                        .map {
                            it.id
                        }
                        .toSet()

                _apoderados.value.filter { apoderado ->
                    apoderado.pupilosIds.any {
                        it in idsEstudiantes
                    }
                }
            }

            AlcanceEnvio.COLEGIO_COMPLETO ->
                _apoderados.value

        }.distinctBy {
            it.id
        }

        if (destinatarios.isEmpty()) {
            return false
        }

        val comunicadoId =
            UUID.randomUUID().toString()

        val fechaActual = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.ROOT
        ).format(Date())

        val comunicado = Comunicacion(
            id = comunicadoId,
            asunto = asunto.trim(),
            cuerpo = cuerpo.trim(),
            tipo = tipo,
            alcance = alcance,
            destinoReferencia = destino,
            adjuntoPdfNombre = adjuntoPdfNombre,
            requiereFirmaFisica = requiereFirmaFisica,
            autorId = usuario.id,
            autorNombre = usuario.nombre,
            fechaHora = fechaActual
        )

        // RF-07 y RF-08: despacho simulado.
        // No se envían correos, SMS ni WhatsApp reales.
        val resultados = destinatarios.map { apoderado ->

            val canal = if (requiereFirmaFisica) {
                CanalDespacho.EMAIL
            } else {
                apoderado.canalPreferente
            }

            EnvioDestinatario(
                id = UUID.randomUUID().toString(),
                comunicacionId = comunicadoId,
                apoderadoId = apoderado.id,
                canal = canal,
                estado = EstadoEnvio.DESPACHADO
            )
        }

        // Guardar comunicado y envíos en una transacción Room.
        return try {

            comunicacionRepository.guardarComunicadoConEnvios(
                comunicacion = comunicado,
                envios = resultados
            )

            true

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e(
                "MPAZ_ROOM",
                "Error al guardar el comunicado",
                e
            )
            false
        }
    }

    // =====================================================================
    // Módulo "Historial y trazabilidad"
    // =====================================================================

    private val _comunicadoSeleccionadoId =
        MutableStateFlow<String?>(null)

    val comunicadoSeleccionadoId: StateFlow<String?> =
        _comunicadoSeleccionadoId.asStateFlow()

    fun seleccionarComunicado(id: String?) {
        _comunicadoSeleccionadoId.value = id
    }

    fun comunicadoPorId(id: String?): Comunicacion? =
        comunicados.value.find {
            it.id == id
        }

    // Función original conservada por compatibilidad.
    fun enviosDe(
        comunicacionId: String
    ): List<EnvioDestinatario> =
        _envios.value.filter {
            it.comunicacionId == comunicacionId
        }

    /**
     * RF-08 y RF-11:
     * Observa los resultados de despacho guardados
     * en Room para cada comunicado.
     */
    fun observarEnvios(
        comunicacionId: String
    ): Flow<List<EnvioDestinatario>> {

        return comunicacionRepository.obtenerEnvios(
            comunicacionId
        )
    }

    /**
     * TODO(equipo): filtrar la bitácora por tipo, emisor y rango de fechas (formato AAAA-MM-DD;
     * null o vacío = sin filtro), del más reciente al más antiguo.
     * RF: "el historial debe poder filtrarse por fecha, tipo de comunicación y emisor".
     * Hoy devuelve la lista completa sin filtrar.
     *
     * Implementado por el equipo.
     */
    fun historialFiltrado(
        tipo: TipoComunicacion?,
        emisorId: String?,
        fechaDesde: String?,
        fechaHasta: String?
    ): List<Comunicacion> {

        return comunicados.value
            .filter { comunicado ->

                val cumpleTipo =
                    tipo == null ||
                            comunicado.tipo == tipo

                val cumpleEmisor =
                    emisorId.isNullOrBlank() ||
                            comunicado.autorId == emisorId

                val fecha =
                    comunicado.fechaHora.take(10)

                val cumpleDesde =
                    fechaDesde.isNullOrBlank() ||
                            fecha >= fechaDesde

                val cumpleHasta =
                    fechaHasta.isNullOrBlank() ||
                            fecha <= fechaHasta

                cumpleTipo &&
                        cumpleEmisor &&
                        cumpleDesde &&
                        cumpleHasta
            }
            .sortedByDescending {
                it.fechaHora
            }
    }

    // =====================================================================
    // Canal preferente del apoderado (supuesto pendiente con el CITT)
    // =====================================================================

    /**
     * TODO(equipo): guardar el canal preferente del apoderado. Criterio de éxito: "emisión
     * simulada inmediata al canal preferente configurado por el apoderado". Hoy no hace nada.
     *
     * Implementado por el equipo utilizando Room.
     */
    fun actualizarCanalPreferente(
        apoderadoId: String,
        canal: CanalDespacho
    ) {
        // Integración del equipo: actualizar el canal en Room.
        viewModelScope.launch {

            try {

                val guardado =
                    apoderadoRepository.actualizarCanal(
                        apoderadoId = apoderadoId,
                        canal = canal
                    )

                if (guardado) {

                    Log.d(
                        "MPAZ_ROOM",
                        "Canal preferente actualizado correctamente"
                    )

                } else {

                    Log.w(
                        "MPAZ_ROOM",
                        "No se encontró el apoderado indicado"
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "MPAZ_ROOM",
                    "Error al guardar el canal preferente",
                    e
                )
            }
        }
    }
}
