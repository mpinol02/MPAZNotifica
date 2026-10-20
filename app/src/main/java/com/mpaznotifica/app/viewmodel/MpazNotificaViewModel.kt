package com.mpaznotifica.app.viewmodel

import androidx.lifecycle.ViewModel
import com.mpaznotifica.app.data.MockData
import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.Apoderado
import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.Comunicacion
import com.mpaznotifica.app.model.EnvioDestinatario
import com.mpaznotifica.app.model.ErroresComunicado
import com.mpaznotifica.app.model.Perfil
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 */
class MpazNotificaViewModel : ViewModel() {

    // =====================================================================
    // Módulo "Autenticación y perfiles"
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * TODO(equipo): inicio de sesión diferenciado para Inspectoría General y Profesor/a Jefe.
     * Validar campos vacíos (patrón Guía 11) y luego comparar contra MockData.usuarios (o el
     * backend que confirme el CITT). Si es correcto: dejar _usuarioActivo y devolver true.
     * Si no: dejar _errorLogin con un mensaje y devolver false.
     */
    fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = "Login sin implementar — ver TODO(equipo) en MpazNotificaViewModel.login()."
        return false
    }

    /**
     * SOLO PARA DESARROLLO: entra con el usuario de prueba del perfil indicado, sin validar
     * credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
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

    private val _apoderados = MutableStateFlow(MockData.apoderados)
    val apoderados: StateFlow<List<Apoderado>> = _apoderados.asStateFlow()

    fun apoderadoPorId(id: String): Apoderado? = _apoderados.value.find { it.id == id }

    fun nombresPupilos(apoderado: Apoderado): List<String> =
        MockData.estudiantes.filter { it.id in apoderado.pupilosIds }.map { "${it.nombreReferencial} (${it.curso})" }

    // =====================================================================
    // Módulo "Redacción y envío"
    // =====================================================================

    /**
     * TODO(equipo): devolver solo los alcances que el perfil puede usar.
     * RF: "restringir las opciones de emisión según el perfil autenticado".
     * Regla de negocio: un Profesor/a Jefe no puede emitir a "colegio completo" ni a un curso
     * distinto al propio (alcance sobre talleres pendiente de confirmar con el CITT).
     * Hoy devuelve los cuatro alcances para cualquier perfil.
     */
    fun alcancesPermitidos(usuario: Usuario?): List<AlcanceEnvio> = AlcanceEnvio.entries.toList()

    /**
     * TODO(equipo): devolver solo los tipos de comunicación que el perfil puede emitir.
     * Criterio de éxito del caso: un Profesor/a Jefe no puede enviar circulares institucionales,
     * solo lo referente a su curso. Hoy devuelve todos los tipos.
     */
    fun tiposPermitidos(usuario: Usuario?): List<TipoComunicacion> = TipoComunicacion.entries.toList()

    /**
     * TODO(equipo): opciones de destino según el alcance, respetando el perfil (por ejemplo, un
     * Profesor/a Jefe solo debería ver su propio curso). Hoy muestra todas las opciones.
     */
    fun destinosPara(alcance: AlcanceEnvio?, usuario: Usuario?): List<String> = when (alcance) {
        AlcanceEnvio.CURSO -> cursos
        AlcanceEnvio.TALLER -> talleres.map { it.nombre }
        AlcanceEnvio.APODERADO_INDIVIDUAL -> _apoderados.value.map { it.nombreCompleto }
        AlcanceEnvio.COLEGIO_COMPLETO, null -> emptyList()
    }

    /**
     * TODO(equipo): validar el formulario y devolver los errores por campo: asunto y cuerpo
     * obligatorios, tipo y alcance elegidos, destino obligatorio salvo "colegio completo", y que
     * el alcance esté permitido para el perfil. Hoy devuelve "sin errores" siempre.
     */
    fun validarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion?,
        alcance: AlcanceEnvio?,
        destino: String?
    ): ErroresComunicado = ErroresComunicado()

    private val _comunicados = MutableStateFlow(MockData.comunicados)
    val comunicados: StateFlow<List<Comunicacion>> = _comunicados.asStateFlow()

    private val _envios = MutableStateFlow(MockData.envios)

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
     */
    fun enviarComunicado(
        asunto: String,
        cuerpo: String,
        tipo: TipoComunicacion,
        alcance: AlcanceEnvio,
        destino: String?,
        adjuntoPdfNombre: String?,
        requiereFirmaFisica: Boolean
    ) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Historial y trazabilidad"
    // =====================================================================

    private val _comunicadoSeleccionadoId = MutableStateFlow<String?>(null)
    val comunicadoSeleccionadoId: StateFlow<String?> = _comunicadoSeleccionadoId.asStateFlow()

    fun seleccionarComunicado(id: String?) {
        _comunicadoSeleccionadoId.value = id
    }

    fun comunicadoPorId(id: String?): Comunicacion? = _comunicados.value.find { it.id == id }

    fun enviosDe(comunicacionId: String): List<EnvioDestinatario> =
        _envios.value.filter { it.comunicacionId == comunicacionId }

    /**
     * TODO(equipo): filtrar la bitácora por tipo, emisor y rango de fechas (formato AAAA-MM-DD;
     * null o vacío = sin filtro), del más reciente al más antiguo.
     * RF: "el historial debe poder filtrarse por fecha, tipo de comunicación y emisor".
     * Hoy devuelve la lista completa sin filtrar.
     */
    fun historialFiltrado(
        tipo: TipoComunicacion?,
        emisorId: String?,
        fechaDesde: String?,
        fechaHasta: String?
    ): List<Comunicacion> = _comunicados.value

    // =====================================================================
    // Canal preferente del apoderado (supuesto pendiente con el CITT)
    // =====================================================================

    /**
     * TODO(equipo): guardar el canal preferente del apoderado. Criterio de éxito: "emisión
     * simulada inmediata al canal preferente configurado por el apoderado". Hoy no hace nada.
     */
    fun actualizarCanalPreferente(apoderadoId: String, canal: CanalDespacho) {
        // Sin implementar todavía.
    }
}
