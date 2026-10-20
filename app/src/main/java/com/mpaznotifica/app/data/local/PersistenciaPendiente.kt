package com.mpaznotifica.app.data.local

// =====================================================================================
// TODO(equipo): PERSISTENCIA LOCAL CON ROOM (exigida por el caso; contenido de la Semana 9)
// =====================================================================================
// Las dependencias de Room, KSP y DataStore YA están configuradas en Gradle. Falta crear
// las clases en esta carpeta y reemplazar las listas de MockData que usa el ViewModel.
//
// 1) ENTIDADES (@Entity) — una por tabla del Modelo Relacional del equipo, por ejemplo:
//      usuarios, apoderados, estudiantes, talleres, comunicaciones y envios
//      (EnvioDestinatario: un registro por destinatario con canal y estado).
//    Relaciones: apoderado 1..N estudiantes (supuesto), estudiante N..N talleres,
//    comunicación 1..N envíos. Los enums se pueden guardar como String (enum.name).
//
// 2) DAOs (@Dao) — consultas que necesita cada pantalla, por ejemplo:
//      - historial ordenado por fecha, filtrable por tipo, emisor y rango de fechas
//      - apoderados de un curso / de un taller (para resolver destinatarios)
//      - envíos de una comunicación
//    Regla: las comunicaciones enviadas son INMUTABLES -> el DAO de comunicaciones solo
//    necesita @Insert y consultas (sin @Update ni @Delete).
//
// 3) BASE DE DATOS (@Database) — AppDatabase con las entidades, version = 1,
//    exportSchema = false, e instancia única.
//
// 4) DATASTORE (opcional) — recordar el último usuario que inició sesión.
//
// 5) VIEWMODEL — reemplazar MutableStateFlow(MockData...) por los Flow del DAO con
//    stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()).
//    Para tener Context, el ViewModel puede extender AndroidViewModel(application).
// =====================================================================================
