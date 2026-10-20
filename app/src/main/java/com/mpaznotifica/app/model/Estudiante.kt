package com.mpaznotifica.app.model

/** Estudiante con identificación referencial FICTICIA, su curso y los talleres a los que asiste. */
data class Estudiante(
    val id: String,
    val nombreReferencial: String,
    val curso: String,
    val talleresIds: List<String> = emptyList()
)

/**
 * Taller extraprogramático. Supuesto: se modela como entidad propia (pendiente de confirmar con
 * el CITT si es entidad o solo una etiqueta del estudiante).
 */
data class Taller(
    val id: String,
    val nombre: String
)
