package com.mpaznotifica.app.model

/**
 * Perfiles emisores del caso (ver Requerimientos_DSY1105-007, "Actores"). El apoderado es solo
 * destinatario: no opera la app, por eso no tiene perfil ni login.
 */
enum class Perfil(val etiqueta: String) {
    INSPECTORIA_GENERAL("Inspectoría General"),
    PROFESOR_JEFE("Profesor/a Jefe")
}
