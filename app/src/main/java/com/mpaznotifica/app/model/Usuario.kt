package com.mpaznotifica.app.model

/**
 * Usuario emisor que inicia sesión. cursoAsignado solo aplica al Profesor/a Jefe (su curso de
 * jefatura). Solo datos ficticios (ver MockData.usuarios).
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil,
    val cursoAsignado: String? = null
)
