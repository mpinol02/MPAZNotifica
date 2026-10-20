package com.mpaznotifica.app.data.remote

// =====================================================================================
// TODO(equipo): DESPACHO SIMULADO Y API REST (Retrofit ya configurado en Gradle)
// =====================================================================================
// El caso pide simular el envío por correo, SMS o WhatsApp con servicios de desarrollo
// (mock), con credenciales y números de PRUEBA definidos por el equipo. Está PROHIBIDO
// conectarse a servidores de correo reales de la municipalidad o a bases productivas.
//
// Sugerencia de estructura en esta carpeta:
//
// 1) DTO del payload de mensajería (los campos que defina el CITT / el backend), por
//    ejemplo: asunto, cuerpo, tipo, canal, destinatario, adjunto.
//
// 2) Interfaz Retrofit, por ejemplo:
//        interface MensajeriaApi {
//            @POST("mensajes")
//            suspend fun enviar(@Body mensaje: MensajeDto): RespuestaEnvioDto
//        }
//
// 3) Una implementación SIMULADA de la misma interfaz (devuelve "despachado" o "fallido"
//    sin llamar a internet) para probar mientras no exista el backend, y la instancia real
//    de Retrofit (Retrofit.Builder + GsonConverterFactory) para cuando el backend exista.
//
// 4) El ViewModel llama al servicio dentro de viewModelScope.launch { } y guarda un
//    EnvioDestinatario por cada apoderado con el estado obtenido.
//
// Pendiente con el CITT: si cada equipo construye el backend Spring Boot o solo lo simula,
// y el formato final del payload de mensajería.
// =====================================================================================
