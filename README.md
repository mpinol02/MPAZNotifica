# MPAZ Notifica — Proyecto base (Sección DSY1105-007, Maipú)

Proyecto Android base para el caso de Vinculación con el Medio **"MPAZ Notifica: Optimización del canal de comunicación móvil y trazabilidad escolar"** de la Escuela Escritora Marcela Paz. Es el punto de partida común de todos los equipos de la sección: trae la estructura, las pantallas de cada módulo, la navegación y la persistencia y la API configuradas. **La lógica de negocio del caso no está implementada**: esa es la parte que construye cada equipo.

Documentación del caso (carpeta `docs/`):

- `Requerimientos_DSY1105-007.pdf`: requerimientos funcionales, reglas de negocio, RNF y supuestos.
- `Consultas_Pendientes_DSY1105-007.pdf`: preguntas abiertas al CITT (Christian Lazcano). No resolver con supuestos propios.

## Cómo abrir y ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. Android Studio → **File > Open…** → seleccionar la carpeta que contiene `settings.gradle.kts`.
3. Esperar el Gradle Sync (la primera vez necesita internet).
4. Ejecutar ▶ **Run 'app'**.
5. En el login, usar **"Modo desarrollo"** para entrar (el login real es tarea del equipo).

Si Android Studio sugiere actualizar Gradle o el Android Gradle Plugin, **no aceptar**: las versiones están fijadas para que todos los equipos usen exactamente las mismas.

Usuarios de prueba (en `data/MockData.kt`, para cuando implementen el login):

| Perfil | Usuario | Clave |
|---|---|---|
| Inspectoría General | inspectoria@mpaznotifica.cl | inspectoria123 |
| Profesor/a Jefe (4° Básico A) | profesor.4basicoA@mpaznotifica.cl | profesor123 |

## Stack (exigido por el caso)

Kotlin 2.0 · Jetpack Compose · Material 3 · MVVM (ViewModel + StateFlow) · Navigation Compose · Room + KSP y DataStore · Retrofit + Gson. Todas las dependencias están configuradas en Gradle. El caso **no** permite usar cámara, micrófono, GPS ni datos biométricos.

## Flujo de pantallas

```
LOGIN ──(login OK)──> PANEL ──> REDACTAR (confirmación + alerta de envío)
                            ──> HISTORIAL (filtros) ──> DETALLE (estado por destinatario)
                            ──> APODERADOS (canal preferente)
Cerrar sesión ──> LOGIN
```

El botón atrás nativo de Android funciona solo (lo maneja el NavHost). La flecha "←" de la barra superior hace lo mismo. Al ingresar, el login sale de la pila; "Cerrar sesión" la limpia completa.

## Estructura del código (`app/src/main/java/com/mpaznotifica/app/`)

| Carpeta | Contenido |
|---|---|
| `model/` | `Comunicacion`, `EnvioDestinatario`, `Apoderado`, `Estudiante`, `Taller`, `Usuario`, `Perfil` y sus enums (tipo, alcance, canal, estado). Incluye `ErroresComunicado` para validar el formulario. |
| `data/` | `MockData.kt`: datos 100 % ficticios. |
| `data/local/` | `PersistenciaPendiente.kt`: guía de qué entidades, DAOs y base de datos crear con Room. |
| `data/remote/` | `ApiPendiente.kt`: guía para el despacho simulado y la API REST con Retrofit. |
| `viewmodel/` | `MpazNotificaViewModel`: un solo ViewModel compartido. Todas las funciones pendientes están marcadas con `TODO(equipo)`. |
| `navigation/` | `AppNavigation`: rutas, NavHost, ingreso y cierre de sesión. |
| `ui/components/` | `BarraSuperior` (flecha atrás y cerrar sesión) y `SelectorDesplegable` (lista desplegable reutilizable). |
| `ui/screen/` | `LoginScreen`, `PanelScreen` y una carpeta por módulo: `comunicados/`, `apoderados/`. |
| `ui/theme/` y `res/` | Tema, colores, logo e ícono **provisionales**. |

## Trazabilidad: requerimiento → pantalla → función a implementar

| Módulo | Requerimiento | Pantalla (ya construida) | Función en el ViewModel (TODO del equipo) |
|---|---|---|---|
| Autenticación y perfiles | Inicio de sesión diferenciado por perfil | `LoginScreen` | `login()` |
| Autenticación y perfiles | Restringir las opciones de emisión según el perfil | `RedactarComunicadoScreen` | `alcancesPermitidos()`, `tiposPermitidos()`, `destinosPara()` |
| Redacción y envío | Redactar con asunto, cuerpo, tipo y PDF opcional | `RedactarComunicadoScreen` (con selector de PDF) | `validarComunicado()` |
| Redacción y envío | Elegir destinatarios: apoderado, curso, taller o colegio | `RedactarComunicadoScreen` | `destinosPara()` |
| Redacción y envío | Inspectoría emite a cualquier alcance; Profesor/a Jefe solo a su curso | `RedactarComunicadoScreen` | `alcancesPermitidos()`, `destinosPara()` |
| Redacción y envío | Simular el despacho por el canal correspondiente | Confirmación y alerta en `RedactarComunicadoScreen` | `enviarComunicado()` + servicio en `data/remote/` |
| Redacción y envío | Registrar el estado del envío por destinatario | `DetalleComunicadoScreen` | `enviarComunicado()` (guardar `EnvioDestinatario`) |
| Historial y trazabilidad | Bitácora de todos los comunicados | `HistorialScreen` | `historialFiltrado()` |
| Historial y trazabilidad | Filtrar por fecha, tipo y emisor | `HistorialScreen` (filtros ya dibujados) | `historialFiltrado()` |
| Historial y trazabilidad | Registro automático de autor, fecha y hora, contenido, destinatarios y canal | `DetalleComunicadoScreen` | `enviarComunicado()` |
| Autorizaciones | Rol solo informativo para autorizaciones con firma legal | Casilla y aviso en `RedactarComunicadoScreen` | `enviarComunicado()` |
| Canal preferente | Envío al canal configurado por el apoderado (supuesto) | `ApoderadosScreen` | `actualizarCanalPreferente()` |
| Persistencia | Room / SQLite (exigido) | — | ver `data/local/PersistenciaPendiente.kt` |

**Reglas de negocio que el equipo debe hacer cumplir:**

- Un Profesor/a Jefe no puede emitir a "colegio completo" ni a un curso distinto del suyo.
- Todo comunicado enviado queda registrado de forma inmutable: no se edita después de enviarlo. Por eso el detalle es solo de lectura.

Para ver todo lo pendiente: **Edit > Find > Find in Files** (Ctrl+Shift+F / Cmd+Shift+F) y buscar `TODO(equipo)`.

## Qué pasa hoy al ejecutar la app (es intencional)

- **Ingresar:** el login siempre rechaza. Hay que entrar con "Modo desarrollo".
- **Restricciones por perfil:** ambos perfiles ven todos los tipos, alcances y destinos.
- **Enviar:** pide confirmación y muestra la alerta, pero el comunicado no aparece en el historial.
- **Historial:** los filtros no filtran.
- **Apoderados:** "Guardar canal" no cambia nada.

## Orden sugerido de trabajo

1. **Login:** validar campos vacíos y luego las credenciales.
2. **Restricciones por perfil:** tipos, alcances y destinos permitidos.
3. **Validación** del formulario de redacción.
4. **Envío:**
   - Registrar el comunicado.
   - Resolver los destinatarios.
   - Simular el despacho por el canal preferente, con `EnvioDestinatario` por cada uno.
   - Incluir el caso de autorizaciones.
5. **Historial:** filtros por fecha, tipo y emisor, ordenado por fecha.
6. **Canal preferente** del apoderado.
7. **Persistencia y API:**
   - Room, exigido por el caso.
   - Servicio simulado con Retrofit, según lo que confirme el CITT sobre el backend.
8. **Cierre:**
   - Quitar el "Modo desarrollo".
   - Revisar que no quede ningún `TODO(equipo)`.
   - Reemplazar el logo y los colores provisionales.

## Reglas del proyecto

- **Datos:** solo datos ficticios, sintéticos y anonimizados (Ley 19.628 y Ley 21.719).
- **Conexiones prohibidas:** servidores de correo reales o bases de datos productivas.
- **Repositorio:**
  - Repositorio **público** en GitHub y tablero Trello.
  - Un integrante sube esta base como commit inicial.
  - Después, una rama por funcionalidad (por ejemplo `feature/login`).
  - Debe haber commits de **ambos** integrantes.
- **Entrega:** APK firmado en modo release y presentación técnica.
- **Consultas sobre el caso:** van por el canal Estudiante → Docente → CITT → organización.

## Logo e ícono

Son **provisionales** (un sobre con un punto de notificación). Para usar el logo oficial:

- **Logo:** copiar el PNG a `res/drawable/logo_app.png` y borrar `logo_app.xml`.
- **Ícono de la app:** clic derecho en `res` > **New > Image Asset**.
