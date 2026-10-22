package com.mpaznotifica.app.data.local

// ============================================================================
// PERSISTENCIA LOCAL CON ROOM - IMPLEMENTACIÓN DEL EQUIPO
// ============================================================================
//
// Este archivo corresponde a la guía de persistencia local
// incluida originalmente en el proyecto base del profesor.
//
// La implementación de Room fue completada por el equipo
// para MPAZ Notifica.
//
// 1. ENTIDADES (@Entity)
//
// Se utilizan las siguientes entidades persistentes:
//
// - ComunicacionEntity:
//   Almacena el contenido del comunicado, su autor,
//   fecha, tipo, alcance y otros datos asociados.
//
// - EnvioEntity:
//   Registra el resultado del despacho simulado
//   por cada apoderado destinatario, incluyendo
//   el canal utilizado y el estado del envío.
//
// - ApoderadoEntity:
//   Guarda los apoderados ficticios, sus datos
//   de contacto, estudiantes asociados
//   y canal preferente de comunicación.
//
// Los usuarios, estudiantes y talleres utilizados
// como datos de referencia permanecen en MockData.
//
// 2. DAO (@Dao)
//
// Las operaciones de almacenamiento y consulta
// se realizan mediante:
//
// - ComunicacionDao:
//   Inserción y consulta de comunicados.
//
// - EnvioDao:
//   Inserción y consulta de resultados de despacho.
//
// - ApoderadoDao:
//   Consulta y actualización de apoderados
//   y sus canales preferentes.
//
// Los comunicados enviados se mantienen inmutables
// dentro del funcionamiento normal de la aplicación.
//
// 3. BASE DE DATOS (@Database)
//
// AppDatabase utiliza Room y SQLite.
//
// - Nombre: mpaz_notifica_db
// - Versión actual: 3
// - Instancia única mediante obtenerInstancia().
//
// Se incorporó una migración de versión 2 a 3
// para agregar la tabla de apoderados.
//
// El registro de un comunicado y sus envíos
// se realiza mediante una transacción Room.
//
// 4. DATASTORE
//
// DataStore fue propuesto como recurso opcional
// en la plantilla original.
//
// La implementación actual no necesita DataStore
// para conservar los comunicados, envíos
// ni los canales preferentes.
//
// Estos datos se almacenan mediante Room.
//
// 5. VIEWMODEL Y REPOSITORIES
//
// MpazNotificaViewModel utiliza AndroidViewModel
// para acceder al contexto de la aplicación.
//
// ComunicacionRepository gestiona el acceso
// a los comunicados y sus resultados de envío.
//
// ApoderadoRepository gestiona los apoderados
// y el almacenamiento del canal preferente.
//
// StateFlow permite que las pantallas observen
// cambios y actualicen su contenido mediante
// collectAsState().
//
// 6. COMPROBACIÓN DE PERSISTENCIA
//
// Se realizaron pruebas para verificar que:
//
// - Los comunicados aparecen en el historial.
// - Los resultados de despacho quedan registrados.
// - Los canales preferentes se almacenan en Room.
// - Los comunicados permanecen después de cerrar
//   y volver a abrir la aplicación.
//
// Esta persistencia corresponde al recurso local
// del dispositivo; no utiliza un servidor remoto.
//
// ============================================================================
// FIN DE LA DOCUMENTACIÓN DE PERSISTENCIA LOCAL
// ============================================================================
