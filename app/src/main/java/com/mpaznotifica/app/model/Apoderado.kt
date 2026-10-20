package com.mpaznotifica.app.model

enum class CanalDespacho(val etiqueta: String) {
    EMAIL("Correo electrónico"),
    SMS("SMS"),
    WHATSAPP("WhatsApp")
}

/**
 * Apoderado (destinatario final, no opera la app). Supuesto pendiente con el CITT: un apoderado
 * puede tener más de un pupilo, y su canal preferente se puede configurar.
 */
data class Apoderado(
    val id: String,
    val nombreCompleto: String,
    val correo: String,
    val telefono: String,
    val canalPreferente: CanalDespacho = CanalDespacho.EMAIL,
    val pupilosIds: List<String> = emptyList()
)
