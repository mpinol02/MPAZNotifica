package com.mpaznotifica.app.model

enum class TipoComunicacion(val etiqueta: String) {
    INSTITUCIONAL("Institucional"),
    PEDAGOGICA("Pedagógica"),
    DISCIPLINARIA("Disciplinaria"),
    CITACION("Citación"),
    EVENTO("Evento"),
    TALLER("Taller"),
    SALIDA_PEDAGOGICA("Salida pedagógica")
}

enum class AlcanceEnvio(val etiqueta: String) {
    APODERADO_INDIVIDUAL("Apoderado individual"),
    CURSO("Curso"),
    TALLER("Taller"),
    COLEGIO_COMPLETO("Colegio completo")
}

enum class EstadoEnvio(val etiqueta: String) {
    DESPACHADO("Despachado"),
    FALLIDO("Fallido")
}

/**
 * Comunicado emitido (entidad "Comunicación" + metadatos de envío del caso).
 *
 * Regla de negocio: una vez enviado queda registrado de forma INMUTABLE (no se edita).
 * requiereFirmaFisica = true marca una autorización con firma legal: la app solo informa y
 * alerta que corresponde entrega física para firma manuscrita (no captura firmas).
 */
data class Comunicacion(
    val id: String,
    val asunto: String,
    val cuerpo: String,
    val tipo: TipoComunicacion,
    val alcance: AlcanceEnvio,
    val destinoReferencia: String? = null,
    val adjuntoPdfNombre: String? = null,
    val requiereFirmaFisica: Boolean = false,
    val autorId: String,
    val autorNombre: String,
    val fechaHora: String
)

/**
 * Resultado del despacho simulado a UN destinatario (RF: "registrar el estado resultante del
 * envío por destinatario").
 */
data class EnvioDestinatario(
    val id: String,
    val comunicacionId: String,
    val apoderadoId: String,
    val canal: CanalDespacho,
    val estado: EstadoEnvio
)

/** Errores de validación del formulario de redacción (patrón Guía 11). */
data class ErroresComunicado(
    val asunto: String? = null,
    val cuerpo: String? = null,
    val tipo: String? = null,
    val alcance: String? = null,
    val destino: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(asunto, cuerpo, tipo, alcance, destino).any { it != null }
}
