
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
import com.mpaznotifica.app.model.Perfil
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.model.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ViewModel principal de MPAZ Notifica.
 *
 * Funcionalidades:
 * - Inicio de sesión diferenciado por perfil.
 * - RF-06: permisos del Profesor Jefe.
 * - Redacción y validación de comunicados.
 * - Despacho simulado DESPACHADO / FALLIDO.
 * - Autorizaciones con firma física en papel.
 * - Almacenamiento de comunicados con Room.
 * - Historial, filtros y trazabilidad.
 * - Actualización del canal preferente.
 *
 * Los despachos son simulados y no envían
 * mensajes reales.
 */
class MpazNotificaViewModel(
    application: Application
) : AndroidViewModel(application) {

    // =====================================================
    // ROOM Y REPOSITORIES
    // =====================================================

    private val database =
        AppDatabase.obtenerInstancia(application)

    private val comunicacionRepository =
        ComunicacionRepository(database)

    private val apoderadoRepository =
        ApoderadoRepository(database.apoderadoDao())

    // =====================================================
    // LOGIN
    // =====================================================

    private val _usuarioActivo =
        MutableStateFlow<Usuario?>(null)

    val usuarioActivo: StateFlow<Usuario?> =
        _usuarioActivo.asStateFlow()

    private val _errorLogin =
        MutableStateFlow<String?>(null)

    val errorLogin: StateFlow<String?> =
        _errorLogin.asStateFlow()

    fun login(
        usuario: String,
        clave: String
    ): Boolean {

        val correo = usuario.trim()

        if (correo.isBlank()) {
            _usuarioActivo.value = null
            _errorLogin.value =
                "Debe ingresar su correo institucional"
            return false
        }

        if (clave.isBlank()) {
            _usuarioActivo.value = null
            _errorLogin.value =
                "Debe ingresar su contraseña"
            return false
        }

        val encontrado = MockData.usuarios.find {
            it.usuario.equals(
                correo,
                ignoreCase = true
            ) && it.clave == clave
        }

        if (encontrado == null) {
            _usuarioActivo.value = null
            _errorLogin.value =
                "Correo o contraseña incorrectos"
            return false
        }

        _usuarioActivo.value = encontrado
        _errorLogin.value = null

        return true
    }

    /**
     * Compatibilidad temporal.
     * Eliminar durante la etapa de limpieza final.
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value =
            MockData.usuarios.first {
                it.perfil == perfil
            }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _comunicadoSeleccionadoId.value = null
    }

    // =====================================================
    // DATOS DE REFERENCIA
    // =====================================================

    val cursos: List<String> =
        MockData.cursos

    val talleres =
        MockData.talleres

    val emisores: List<Usuario> =
        MockData.usuarios

    private val _apoderados =
        MutableStateFlow(MockData.apoderados)

    val apoderados: StateFlow<List<Apoderado>> =
        _apoderados.asStateFlow()

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
                    "Error al cargar apoderados",
                    e
                )
            }
        }
    }

    fun apoderadoPorId(
        id: String
    ): Apoderado? {
        return _apoderados.value.find {
            it.id == id
        }
    }

    fun nombresPupilos(
        apoderado: Apoderado
    ): List<String> {

        return MockData.estudiantes
            .filter {
                it.id in apoderado.pupilosIds
            }
            .map {
                "${it.nombreReferencial} (${it.curso})"
            }
    }

    // =====================================================
    // RF-06: PERMISOS POR PERFIL
    // =====================================================

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
     * RF-06:
     * El Profesor Jefe puede elegir su curso
     * y apoderados individuales de ese curso.
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

                    val idsDelCurso =
                        MockData.estudiantes
                            .filter {
                                it.curso == usuario.cursoAsignado
                            }
                            .map { it.id }
                            .toSet()

                    _apoderados.value
                        .filter { apoderado ->
                            apoderado.pupilosIds.any {
                                it in idsDelCurso
                            }
                        }
                        .map { it.nombreCompleto }

                } else {
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

    // =====================================================
    // VALIDACIONES
    // =====================================================

    fun validarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion?,
        alcance: AlcanceEnvio?,
        destino: String?
    ): ErroresComunicado {

        val usuario = _usuarioActivo.value

        val errorAsunto =
            if (asunto.isBlank()) {
                "Debe ingresar el asunto"
            } else {
                null
            }

        val errorCuerpo =
            if (cuerpo.isBlank()) {
                "Debe ingresar el mensaje"
            } else {
                null
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

            destino !in destinosPara(
                alcance,
                usuario
            ) -> {
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

    // =====================================================
    // COMUNICADOS DESDE ROOM
    // =====================================================

    val comunicados: StateFlow<List<Comunicacion>> =
        comunicacionRepository.comunicados.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _envios =
        MutableStateFlow(MockData.envios)

    // =====================================================
    // ENVÍO DE COMUNICADOS
    // =====================================================

    suspend fun enviarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion,
        alcance: AlcanceEnvio,
        destino: String?,
        adjuntoPdfNombre: String?,
        requiereFirmaFisica: Boolean
    ): Boolean {

        // Validar primero el formulario y los permisos.
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

        // =================================================
        // RESOLVER DESTINATARIOS
        // =================================================

        val destinatarios = when (alcance) {

            AlcanceEnvio.APODERADO_INDIVIDUAL -> {
                _apoderados.value.filter {
                    it.nombreCompleto == destino
                }
            }

            AlcanceEnvio.CURSO -> {

                val idsDelCurso =
                    MockData.estudiantes
                        .filter {
                            it.curso == destino
                        }
                        .map { it.id }
                        .toSet()

                _apoderados.value.filter { apoderado ->
                    apoderado.pupilosIds.any {
                        it in idsDelCurso
                    }
                }
            }

            AlcanceEnvio.TALLER -> {

                val taller = talleres.find {
                    it.nombre == destino
                } ?: return false

                val idsDelTaller =
                    MockData.estudiantes
                        .filter {
                            taller.id in it.talleresIds
                        }
                        .map { it.id }
                        .toSet()

                _apoderados.value.filter { apoderado ->
                    apoderado.pupilosIds.any {
                        it in idsDelTaller
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

        // =================================================
        // FIRMA FÍSICA
        // =================================================

        val textoFirma =
            "IMPORTANTE: Esta autorización requiere " +
                    "firma física del apoderado. " +
                    "Debe firmar el documento en papel " +
                    "y entregarlo al establecimiento."

        val cuerpoFinal =
            if (requiereFirmaFisica) {
                "${cuerpo.trim()}\n\n$textoFirma"
            } else {
                cuerpo.trim()
            }

        val comunicado = Comunicacion(
            id = comunicadoId,
            asunto = asunto.trim(),
            cuerpo = cuerpoFinal,
            tipo = tipo,
            alcance = alcance,
            destinoReferencia = destino,
            adjuntoPdfNombre = adjuntoPdfNombre,
            requiereFirmaFisica = requiereFirmaFisica,
            autorId = usuario.id,
            autorNombre = usuario.nombre,
            fechaHora = fechaActual
        )

        // =================================================
        // DESPACHO SIMULADO: DESPACHADO O FALLIDO
        // =================================================

        val resultados = destinatarios.map { apoderado ->

            // Las autorizaciones con firma física
            // se informan por EMAIL.
            val canal =
                if (requiereFirmaFisica) {
                    CanalDespacho.EMAIL
                } else {
                    apoderado.canalPreferente
                }

            // El servicio revisa si hay correo
            // o teléfono para utilizar el canal.
            val estado = DespachoSimulado.estadoPara(
                canal = canal,
                correo = apoderado.correo,
                telefono = apoderado.telefono
            )

            EnvioDestinatario(
                id = UUID.randomUUID().toString(),
                comunicacionId = comunicadoId,
                apoderadoId = apoderado.id,
                canal = canal,
                estado = estado
            )
        }

        // =================================================
        // GUARDAR COMUNICADO Y ENVÍOS EN ROOM
        // =================================================

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
                "Error al guardar comunicado y envíos",
                e
            )

            false
        }
    }

    // =====================================================
    // HISTORIAL Y TRAZABILIDAD
    // =====================================================

    private val _comunicadoSeleccionadoId =
        MutableStateFlow<String?>(null)

    val comunicadoSeleccionadoId: StateFlow<String?> =
        _comunicadoSeleccionadoId.asStateFlow()

    fun seleccionarComunicado(id: String?) {
        _comunicadoSeleccionadoId.value = id
    }

    fun comunicadoPorId(
        id: String?
    ): Comunicacion? {
        return comunicados.value.find {
            it.id == id
        }
    }

    fun enviosDe(
        comunicacionId: String
    ): List<EnvioDestinatario> {
        return _envios.value.filter {
            it.comunicacionId == comunicacionId
        }
    }

    fun observarEnvios(
        comunicacionId: String
    ): Flow<List<EnvioDestinatario>> {
        return comunicacionRepository.obtenerEnvios(
            comunicacionId
        )
    }

    // =====================================================
    // FILTROS DEL HISTORIAL
    // =====================================================

    fun historialFiltrado(
        tipo: TipoComunicacion?,
        emisorId: String?,
        fechaDesde: String?,
        fechaHasta: String?
    ): List<Comunicacion> {

        return comunicados.value
            .filter { comunicado ->

                val fecha =
                    comunicado.fechaHora.take(10)

                val coincideTipo =
                    tipo == null ||
                            comunicado.tipo == tipo

                val coincideEmisor =
                    emisorId.isNullOrBlank() ||
                            comunicado.autorId == emisorId

                val coincideDesde =
                    fechaDesde.isNullOrBlank() ||
                            fecha >= fechaDesde

                val coincideHasta =
                    fechaHasta.isNullOrBlank() ||
                            fecha <= fechaHasta

                coincideTipo &&
                        coincideEmisor &&
                        coincideDesde &&
                        coincideHasta
            }
            .sortedByDescending {
                it.fechaHora
            }
    }

    // =====================================================
    // CANAL PREFERENTE DEL APODERADO
    // =====================================================

    fun actualizarCanalPreferente(
        apoderadoId: String,
        canal: CanalDespacho
    ) {

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
                        "Canal preferente actualizado"
                    )
                } else {
                    Log.w(
                        "MPAZ_ROOM",
                        "Apoderado no encontrado"
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "MPAZ_ROOM",
                    "Error al actualizar canal",
                    e
                )
            }
        }
    }
}
