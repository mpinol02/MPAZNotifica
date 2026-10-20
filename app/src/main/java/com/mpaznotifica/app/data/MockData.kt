package com.mpaznotifica.app.data

import com.mpaznotifica.app.model.AlcanceEnvio
import com.mpaznotifica.app.model.Apoderado
import com.mpaznotifica.app.model.CanalDespacho
import com.mpaznotifica.app.model.Comunicacion
import com.mpaznotifica.app.model.EnvioDestinatario
import com.mpaznotifica.app.model.EstadoEnvio
import com.mpaznotifica.app.model.Estudiante
import com.mpaznotifica.app.model.Perfil
import com.mpaznotifica.app.model.Taller
import com.mpaznotifica.app.model.TipoComunicacion
import com.mpaznotifica.app.model.Usuario

/**
 * Datos 100% FICTICIOS (Ley 19.628 y Ley 21.719: prohibido usar datos reales de estudiantes o
 * apoderados). Sirven para que las pantallas tengan algo que mostrar mientras el equipo
 * implementa Room y la API simulada.
 */
object MockData {

    /** Usuarios de prueba para el login. MpazNotificaViewModel.login() todavía NO los usa. */
    val usuarios = listOf(
        Usuario("U1", "Inspectoría General (demo)", "inspectoria@mpaznotifica.cl", "inspectoria123", Perfil.INSPECTORIA_GENERAL),
        Usuario("U2", "Profesor/a Jefe 4° A (demo)", "profesor.4basicoA@mpaznotifica.cl", "profesor123", Perfil.PROFESOR_JEFE, "4° Básico A"),
    )

    val cursos = listOf("1° Básico A", "2° Básico A", "3° Básico A", "4° Básico A", "5° Básico A", "8° Básico A")

    val talleres = listOf(
        Taller("T1", "Taller de Robótica"),
        Taller("T2", "Taller de Fútbol"),
    )

    val estudiantes = listOf(
        Estudiante("E1", "Estudiante Ficticio 1", "4° Básico A", listOf("T1")),
        Estudiante("E2", "Estudiante Ficticio 2", "4° Básico A"),
        Estudiante("E3", "Estudiante Ficticio 3", "2° Básico A", listOf("T2")),
    )

    val apoderados = listOf(
        Apoderado("AP1", "Apoderado Ficticio 1", "apoderado1@ejemplo.cl", "+56900000001", CanalDespacho.EMAIL, listOf("E1")),
        Apoderado("AP2", "Apoderado Ficticio 2", "apoderado2@ejemplo.cl", "+56900000002", CanalDespacho.WHATSAPP, listOf("E2")),
        Apoderado("AP3", "Apoderado Ficticio 3", "apoderado3@ejemplo.cl", "+56900000003", CanalDespacho.SMS, listOf("E3")),
    )

    val comunicados = listOf(
        Comunicacion(
            id = "C1", asunto = "Reunión de apoderados 4° Básico A",
            cuerpo = "Se cita a reunión de apoderados el viernes a las 18:00 hrs.",
            tipo = TipoComunicacion.CITACION, alcance = AlcanceEnvio.CURSO, destinoReferencia = "4° Básico A",
            autorId = "U2", autorNombre = "Profesor/a Jefe 4° A (demo)", fechaHora = "2026-09-01 10:00"
        ),
        Comunicacion(
            id = "C2", asunto = "Salida pedagógica al museo",
            cuerpo = "Se informa salida pedagógica. La autorización debe firmarse en papel y devolverse al profesor jefe.",
            tipo = TipoComunicacion.SALIDA_PEDAGOGICA, alcance = AlcanceEnvio.COLEGIO_COMPLETO,
            requiereFirmaFisica = true,
            autorId = "U1", autorNombre = "Inspectoría General (demo)", fechaHora = "2026-09-15 09:30"
        ),
    )

    val envios = listOf(
        EnvioDestinatario("EV1", "C1", "AP1", CanalDespacho.EMAIL, EstadoEnvio.DESPACHADO),
        EnvioDestinatario("EV2", "C1", "AP2", CanalDespacho.WHATSAPP, EstadoEnvio.FALLIDO),
    )
}
