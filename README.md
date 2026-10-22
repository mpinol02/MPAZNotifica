# MPAZ Notifica — Sección DSY1105-007, Maipú

Proyecto Android desarrollado a partir de la base entregada por el profesor para el caso de Vinculación con el Medio **"MPAZ Notifica: Optimización del canal de comunicación móvil y trazabilidad escolar"** de la Escuela Escritora Marcela Paz.

El proyecto conserva la estructura y las pantallas originales, incorporando la lógica de negocio, las restricciones por perfil, la persistencia local con Room, las validaciones, el historial de comunicaciones y las pruebas realizadas por el equipo.

**Estado actual:** aplicación funcional, utilizando exclusivamente datos ficticios y simulación de despachos. No se realizan envíos reales de correo electrónico, SMS ni WhatsApp.

Documentación del caso (carpeta `docs/`):

- `Requerimientos_DSY1105-007.pdf`: requerimientos funcionales, reglas de negocio, RNF y supuestos.
- `Consultas_Pendientes_DSY1105-007.pdf`: preguntas abiertas al CITT (Christian Lazcano). No resolver con supuestos propios.

---

## Cómo abrir y ejecutar

1. Descomprimir el ZIP en una carpeta limpia.
2. Abrir Android Studio.
3. Seleccionar **File > Open…** y elegir la carpeta que contiene `settings.gradle.kts`.
4. Esperar a que termine la sincronización de Gradle.
5. Seleccionar un emulador Android.
6. Ejecutar **Run 'app'**.
7. Iniciar sesión utilizando las credenciales ficticias indicadas abajo.

**Importante:** no actualizar las versiones de Gradle ni del Android Gradle Plugin sin necesidad, ya que están configuradas para el proyecto.

Los botones de acceso de **Modo desarrollo** fueron retirados de la pantalla de inicio de sesión. El ingreso se realiza mediante la validación de credenciales.

### Usuarios de prueba

Los usuarios se encuentran definidos en `data/MockData.kt`.

| Perfil | Usuario | Clave |
|---|---|---|
| Inspectoría General | `inspectoria@mpaznotifica.cl` | `inspectoria123` |
| Profesor/a Jefe (4° Básico A) | `profesor.4basicoA@mpaznotifica.cl` | `profesor123` |

Los apoderados son destinatarios finales y no inician sesión en la aplicación.

---

## Stack tecnológico

El proyecto utiliza las tecnologías indicadas en la base del profesor:

- **Kotlin 2.0:** lenguaje de programación.
- **Jetpack Compose:** construcción de las interfaces.
- **Material 3:** componentes y diseño visual.
- **MVVM:** separación de interfaz, lógica y datos.
- **ViewModel y StateFlow:** gestión del estado.
- **Navigation Compose:** navegación entre pantallas.
- **Room y SQLite:** persistencia de información local.
- **KSP:** procesamiento de anotaciones.
- **DataStore, Retrofit y Gson:** dependencias configuradas en el proyecto base.

**Nota:** el despacho actual es simulado mediante lógica local. No requiere un backend ni utiliza servicios externos de mensajería.

El caso no permite utilizar cámara, micrófono, GPS ni datos biométricos.

---

## Flujo de pantallas

```text
LOGIN ──(login OK)──> PANEL
                         |
                         +──> REDACTAR
                         |       |
                         |       +──> Validaciones
                         |       +──> Selector de PDF
                         |       +──> Autorización con firma física
                         |       +──> Confirmación y envío simulado
                         |       +──> Notificación local de registro
                         |
                         +──> HISTORIAL
                         |       |
                         |       +──> Filtros
                         |       +──> DETALLE
                         |               |
                         |               +──> Destinatarios
                         |               +──> Canales y estados
                         |
                         +──> APODERADOS
                                 |
                                 +──> Canal preferente

Cerrar sesión ──> LOGIN
```

La navegación utiliza `NavHost` y los controles de retroceso de Android.

Al ingresar, el login sale de la pila de navegación. Al cerrar sesión, la aplicación regresa al login e impide volver a una pantalla protegida sin autenticarse nuevamente.

---

## Estructura del código

Ruta principal:

`app/src/main/java/com/mpaznotifica/app/`

| Carpeta | Contenido |
|---|---|
| `model/` | Modelos `Comunicacion`, `EnvioDestinatario`, `Apoderado`, `Estudiante`, `Taller`, `Usuario`, `Perfil` y sus enumeraciones. |
| `data/` | `MockData.kt`: datos ficticios para las demostraciones. |
| `data/local/` | Entidades, DAO y base de datos Room. |
| `data/repository/` | Repositorios de comunicaciones y apoderados. |
| `data/remote/` | `ApiPendiente.kt`: guía original para una futura integración con servicios externos. |
| `viewmodel/` | `MpazNotificaViewModel`: lógica de negocio, validaciones, permisos y StateFlow; `DespachoSimulado.kt`: evaluación local del estado por canal. |
| `navigation/` | `AppNavigation`: rutas, NavHost y navegación. |
| `ui/components/` | `BarraSuperior` y `SelectorDesplegable`. |
| `ui/screen/` | Pantallas de login, panel, comunicados, historial, detalle y apoderados. |
| `ui/theme/` y `res/` | Tema, colores, logo e ícono del proyecto. |
| Raíz del paquete | `NotificacionLocal.kt`: segundo recurso nativo de Android. |

### Persistencia local con Room

Los archivos utilizados para la persistencia incluyen:

| Archivo | Función |
|---|---|
| `AppDatabase.kt` | Configuración de Room y acceso a los DAO. |
| `ComunicacionEntity.kt` | Entidad para almacenar comunicados. |
| `ComunicacionDao.kt` | Consultas y operaciones sobre comunicados. |
| `EnvioEntity.kt` | Entidad de resultados de envío. |
| `EnvioDao.kt` | Consultas y operaciones sobre destinatarios. |
| `ApoderadoEntity.kt` | Entidad para almacenar apoderados. |
| `ApoderadoDao.kt` | Consultas y actualización de canales. |
| `ComunicacionRepository.kt` | Acceso y transformación de datos de comunicados. |
| `ApoderadoRepository.kt` | Acceso y transformación de datos de apoderados. |

Se conserva `PersistenciaPendiente.kt` como documentación actualizada a partir de la guía original del profesor.

El esquema de Room utiliza una migración de la versión 2 a la 3 para agregar los apoderados sin eliminar los comunicados previamente almacenados en esa ruta de actualización.

---

## Arquitectura MVVM

Se utiliza el patrón **Model-View-ViewModel**.

El flujo general es:

```text
INTERFAZ (Jetpack Compose)
           |
           v
VIEWMODEL (StateFlow y validaciones)
           |
           v
REPOSITORY (acceso a datos)
           |
           v
DAO (consultas Room)
           |
           v
BASE DE DATOS SQLITE
```

### Responsabilidades

| Capa | Responsabilidad |
|---|---|
| Model | Representar usuarios, comunicados, apoderados y envíos. |
| View | Mostrar información y recibir acciones del usuario. |
| ViewModel | Gestionar el estado, validar formularios y aplicar permisos. |
| Repository | Intermediar entre ViewModel y persistencia local. |
| DAO | Ejecutar las operaciones sobre las tablas de Room. |

El ViewModel utiliza `StateFlow` para mantener información observable desde las pantallas mediante `collectAsState()`.

---

## Trazabilidad: requerimiento → pantalla → implementación

| Módulo | Requerimiento | Pantalla | Función o implementación |
|---|---|---|---|
| Autenticación y perfiles | Inicio de sesión diferenciado por perfil | `LoginScreen` | `login()` |
| Autenticación y perfiles | Restringir las opciones de emisión según el perfil | `RedactarComunicadoScreen` | `alcancesPermitidos()`, `tiposPermitidos()`, `destinosPara()` |
| Redacción y envío | Redactar asunto, cuerpo, tipo y adjunto PDF opcional | `RedactarComunicadoScreen` | `validarComunicado()` y selector PDF |
| Redacción y envío | Elegir destinatarios: apoderado, curso, taller o colegio | `RedactarComunicadoScreen` | `destinosPara()` |
| Redacción y envío | Inspectoría emite a distintos alcances y Profesor Jefe solo a su curso y a los apoderados individuales de ese curso | `RedactarComunicadoScreen` | `alcancesPermitidos()`, `destinosPara()` y `validarComunicado()` |
| Redacción y envío | Simular el despacho por el canal correspondiente | `RedactarComunicadoScreen` | `enviarComunicado()` |
| Redacción y envío | Registrar `DESPACHADO` o `FALLIDO` según los datos de contacto | `DetalleComunicadoScreen` | `DespachoSimulado.estadoPara()`, `EnvioDestinatario` y Room |
| Historial y trazabilidad | Bitácora de comunicados | `HistorialScreen` | Flow de Room |
| Historial y trazabilidad | Filtrar por fecha, tipo y emisor | `HistorialScreen` | `historialFiltrado()` |
| Historial y trazabilidad | Registrar autor, fecha, contenido, destinatarios y canal | `DetalleComunicadoScreen` | `enviarComunicado()` y Repository |
| Autorizaciones | Avisar cuando se requiere firma física y agregar la indicación de firma en papel al cuerpo | `RedactarComunicadoScreen` | Casilla, aviso animado y `enviarComunicado()` |
| Recursos nativos | Mostrar notificación al registrar exitosamente un comunicado | `RedactarComunicadoScreen` | `NotificacionLocal.mostrarComunicadoRegistrado()` |
| Canal preferente | Configurar canal del apoderado | `ApoderadosScreen` | `actualizarCanalPreferente()` |
| Persistencia | Guardar datos con Room / SQLite | Diversas pantallas | Entity, DAO, Database y Repository |

### Reglas de negocio implementadas

- Inspectoría General puede emitir comunicados utilizando los alcances autorizados.
- El Profesor/a Jefe puede seleccionar su curso asignado y los apoderados individuales asociados a ese curso (AP1 y AP2 en los datos ficticios). No puede enviar a otros cursos ni a sus apoderados.
- Las validaciones del ViewModel también comprueban los permisos antes de guardar.
- El formulario exige los campos obligatorios.
- Los comunicados enviados quedan registrados y no pueden editarse después de su envío.
- Los resultados de despacho se almacenan por destinatario, con estado `DESPACHADO` o `FALLIDO` según exista correo o teléfono para el canal elegido.
- Si una autorización requiere firma física, se añade una indicación al final del cuerpo sobre la firma en papel.
- Los canales preferentes se guardan con Room.
- El historial permite filtrar los comunicados.

Los aspectos pendientes de validación con el CITT se mantienen como supuestos y no deben considerarse confirmaciones oficiales.

---

## Funcionamiento actual de la aplicación

A diferencia del proyecto base inicial, el equipo implementó la lógica funcional de los módulos principales.

| Funcionalidad | Estado actual |
|---|---|
| Login con credenciales ficticias | Implementado y probado |
| Validación de correo y contraseña | Implementado y probado |
| Restricciones por perfil, incluido apoderado individual del curso del Profesor Jefe | Implementado y probado desde la interfaz |
| Redacción de comunicados | Implementado y probado |
| Validación del formulario | Implementado y probado |
| Confirmación antes del envío | Implementado y probado |
| Despacho simulado por canal y estados `DESPACHADO`/`FALLIDO` | Implementado; seis pruebas automatizadas de reglas aprobadas |
| Historial de comunicados | Implementado y probado |
| Filtros por fecha, tipo y autor | Implementado y probado |
| Detalle y estado por destinatario | Implementado y probado |
| Persistencia de comunicados en Room | Implementado y probado |
| Persistencia de canales preferentes | Implementado y probado |
| Selector nativo de PDF | Implementado y probado |
| Notificación local de Android y solicitud de permiso en Android 13+ | Implementado y probado en emulador |
| Texto de firma física incorporado al cuerpo | Implementado y comprobado en historial |
| Animación de aviso de firma física | Implementado y probado |
| Pruebas automatizadas | 16 pruebas aprobadas: 5 ViewModel, 5 Room y 6 despacho simulado |

### Limitaciones del MVP académico

- No se envían mensajes reales por correo, SMS ni WhatsApp.
- Los destinatarios utilizados son ficticios.
- El despacho es una simulación local: `DESPACHADO` indica que existe un dato de contacto para el canal utilizado, no que el mensaje se entregó realmente. `FALLIDO` indica ausencia de correo para EMAIL o de teléfono para SMS/WhatsApp. No se simulan errores externos de proveedores.
- El selector PDF permite escoger un documento, pero actualmente se registra su identificador o nombre referencial y no el contenido completo del archivo.
- Los apoderados no utilizan la aplicación directamente.
- No se implementa firma digital ni captura de firmas.
- No se requiere conexión con sistemas escolares reales.

---

## Recursos nativos de Android — Criterio G6

Se implementaron dos recursos nativos: selector de documentos PDF y notificaciones locales.

### Recurso 1: selector nativo de PDF

Se implementó el selector nativo de documentos mediante:

`ActivityResultContracts.OpenDocument()`

### Funcionamiento

1. El usuario abre la pantalla de redacción.
2. Presiona **Adjuntar PDF (opcional)**.
3. Android muestra el selector de documentos.
4. El usuario selecciona un PDF.
5. La aplicación muestra una referencia del documento seleccionado.
6. El usuario puede cambiar o quitar el adjunto.

### Justificación

El recurso es pertinente para MPAZ Notifica porque los comunicados escolares pueden requerir documentos informativos o autorizaciones.

Se utiliza el selector del sistema Android, sin solicitar permisos innecesarios de cámara, micrófono o ubicación.

**Alcance actual:** se conserva una referencia textual del documento, no una copia persistente del PDF.

### Recurso 2: notificación local

Se implementó una notificación del sistema mediante `NotificacionLocal.kt`, con `NotificationChannel` en las versiones que lo requieren.

1. El usuario confirma el comunicado.
2. El ViewModel registra el comunicado y los resultados del despacho simulado en Room.
3. Si el guardado fue exitoso, la aplicación muestra una notificación local con el asunto.
4. En Android 13 o superior, cuando corresponde, solicita el permiso `POST_NOTIFICATIONS`.

**Alcance actual:** la notificación confirma el registro local del comunicado. No indica una entrega real a los apoderados. Si el permiso es rechazado, el comunicado igualmente permanece guardado.

---

## Animaciones y componentes interactivos — Criterio G7

Se implementó una animación utilizando `AnimatedVisibility` de Jetpack Compose.

### Funcionamiento

En la pantalla `RedactarComunicadoScreen`, al activar la casilla:

**"Es una autorización que requiere firma física del apoderado"**

Aparece una tarjeta informativa.

Al desmarcar la casilla, la tarjeta desaparece.

Las transiciones utilizadas son:

- `fadeIn`: aparición suave.
- `expandVertically`: expansión vertical.
- `fadeOut`: desaparición suave.
- `shrinkVertically`: contracción vertical.

Se utilizan transiciones de aproximadamente 300 ms al aparecer y 250 ms al desaparecer.

Esta animación mejora la experiencia del usuario porque presenta información adicional únicamente cuando corresponde.

Cuando se marca la casilla, el despacho utiliza el canal EMAIL y el cuerpo guardado incorpora al final una indicación de firma en papel y entrega al establecimiento. La aplicación no captura firmas digitales.

La aplicación utiliza además componentes interactivos de Material 3, como campos de texto, listas desplegables, casillas, botones y cuadros de diálogo.

---

## Pruebas realizadas

Se ejecutaron pruebas manuales y automatizadas para verificar el funcionamiento del proyecto.

### Pruebas funcionales manuales

| Área evaluada | Resultado |
|---|---|
| Login, credenciales y cierre de sesión | 5/5 aprobadas |
| Permisos, validaciones y envío | 5/5 aprobadas |
| Historial, filtros y persistencia | 5/5 aprobadas |
| PDF, animación, navegación y estabilidad | 5/5 aprobadas |
| **Total** | **20/20 aprobadas** |

### Pruebas adicionales de casos límite

| Prueba | Resultado |
|---|---|
| Restricción del Profesor Jefe a su curso | Aprobada desde la interfaz |
| Restricción de acceso sin sesión | Aprobada desde la interfaz |
| Rechazo de comunicados inválidos | Aprobada |
| Comprobación de registros no duplicados | Aprobada |
| Conservación de registros anteriores | Aprobada |

**Total:** 5 pruebas adicionales realizadas correctamente.

### Pruebas automatizadas del ViewModel

Archivo:

`app/src/androidTest/java/com/mpaznotifica/app/MpazNotificaViewModelTest.kt`

| Prueba | Resultado |
|---|---|
| Profesor no puede validar otro curso | Aprobada |
| No permitir comunicado sin sesión | Aprobada |
| Rechazar campos vacíos | Aprobada |
| Inspectoría dispone de cuatro alcances | Aprobada |
| Profesor puede utilizar su propio curso | Aprobada |

**Resultado:** 5/5 pruebas aprobadas.

### Pruebas automatizadas de Room

Archivo:

`app/src/androidTest/java/com/mpaznotifica/app/AppDatabaseTest.kt`

| Prueba | Resultado |
|---|---|
| Guardar y recuperar comunicado | Aprobada |
| Guardar comunicado con envíos | Aprobada |
| Actualizar canal preferente | Aprobada |
| Mantener canal preferente al reinicializar | Aprobada |
| Rechazar envío asociado a comunicado inexistente | Aprobada |

**Resultado:** 5/5 pruebas aprobadas.

Las pruebas de Room utilizan una base de datos temporal en memoria para no modificar los datos normales de la aplicación.

### Pruebas automatizadas del despacho simulado

Archivo:

`app/src/androidTest/java/com/mpaznotifica/app/DespachoSimuladoTest.kt`

| Prueba | Resultado |
|---|---|
| EMAIL con correo | `DESPACHADO` |
| EMAIL sin correo | `FALLIDO` |
| SMS con teléfono | `DESPACHADO` |
| SMS sin teléfono | `FALLIDO` |
| WHATSAPP con teléfono | `DESPACHADO` |
| WHATSAPP sin teléfono | `FALLIDO` |

**Resultado:** 6/6 pruebas aprobadas. Estas pruebas verifican la lógica del simulador; aún no constituyen una prueba de integración que confirme la persistencia de un `FALLIDO` en Room.

### Resumen de pruebas

| Categoría | Aprobadas |
|---|---|
| Funcionales manuales | 20 |
| Adicionales de casos límite | 5 |
| Automatizadas del ViewModel | 5 |
| Automatizadas de Room | 5 |
| Automatizadas del despacho simulado | 6 |
| **Total** | **41** |

Las pruebas fueron ejecutadas en Android Studio utilizando un emulador Pixel 10 Pro XL.

Estos resultados corresponden a las verificaciones realizadas por el equipo y no sustituyen la evaluación técnica del profesor.

---

## Orden sugerido de trabajo del profesor

Se conserva el orden de trabajo propuesto originalmente, indicando los avances.

1. **Login:** implementado.
2. **Restricciones por perfil:** implementadas.
3. **Validación del formulario:** implementada.
4. **Envío:**
    - Registrar el comunicado: implementado.
    - Resolver los destinatarios: implementado.
    - Simular el despacho por canal preferente: implementado, con estados `DESPACHADO` y `FALLIDO` por disponibilidad de contacto.
    - Incluir autorizaciones: aviso informativo y texto de firma en papel incorporado al cuerpo.
5. **Historial:** filtros por fecha, tipo y emisor implementados.
6. **Canal preferente:** guardado mediante Room.
7. **Persistencia y API:**
    - Room: implementado.
    - Servicio externo Retrofit: no utilizado en el MVP actual; integración pendiente de confirmación con el CITT.
8. **Cierre:**
    - Botones de Modo desarrollo retirados de la interfaz.
    - Comentarios pendientes de la plantilla revisados y documentación actualizada en Kotlin, XML y README.
    - Logo y colores de la propuesta académica conservados, sin atribuir carácter oficial a la paleta.
    - Segundo recurso nativo incorporado: notificación local al registrar comunicados.

---

## Reglas del proyecto

Se mantienen las indicaciones originales del profesor.

- **Datos:** solo datos ficticios, sintéticos y anonimizados, respetando las exigencias de protección de datos personales.
- **Conexiones prohibidas:** servidores de correo reales o bases de datos productivas.
- **Repositorio:**
    - Repositorio público en GitHub y tablero Trello, según las instrucciones del profesor.
    - Un integrante sube la base como commit inicial.
    - Se recomienda trabajar mediante ramas por funcionalidad.
    - Debe haber commits de ambos integrantes del equipo.
- **Consultas sobre el caso:** Estudiante → Docente → CITT → organización.

### Entrega de la Evaluación Parcial 2

De acuerdo con la rúbrica de esta evaluación, se debe entregar un ZIP del proyecto Android completo.

El ZIP debe:

1. Contener la carpeta principal donde se encuentra `settings.gradle.kts`.
2. Incluir el código fuente Kotlin.
3. Incluir los archivos Gradle.
4. Incluir `README.md` y la documentación correspondiente.
5. No incluir carpetas generadas como `build/` ni `.gradle/`.
6. Identificar a los integrantes según las instrucciones de entrega.

**Formato indicado por la rúbrica:**

`EP2_<sección>_<apellido1>_<apellido2>.zip`

El proyecto se entrega por AVA dentro del plazo indicado por el profesor, antes de la defensa.

La documentación original del proyecto base también contempla un APK firmado en modo release y una presentación técnica. Se conserva esta referencia; para la Evaluación Parcial 2 se priorizan las instrucciones específicas de su rúbrica.

---

## Logo e ícono

Se conservan los recursos provisionales del proyecto base: un sobre con un punto de notificación.

Si posteriormente se autoriza el uso del logo oficial:

- **Logo:** copiar el PNG a `res/drawable/logo_app.png` y reemplazar el recurso provisional.
- **Ícono de la aplicación:** utilizar **New > Image Asset** desde Android Studio.

No se utilizan datos ni recursos institucionales no autorizados.

---

## Estado final del desarrollo

MPAZ Notifica cuenta actualmente con:

- Inicio de sesión funcional.
- Permisos diferenciados entre Inspectoría General y Profesor Jefe.
- Redacción y validación de comunicados.
- Simulación de despacho por canales, con estados `DESPACHADO` y `FALLIDO`.
- Historial con filtros.
- Trazabilidad por destinatario.
- Persistencia local mediante Room.
- Configuración persistente del canal preferente.
- Selector nativo de documentos PDF.
- Notificación local del sistema Android tras el guardado exitoso.
- Profesor Jefe autorizado a comunicarse con su curso y apoderados individuales de ese curso.
- Texto de firma en papel incorporado a las autorizaciones correspondientes.
- Animación informativa para autorizaciones con firma física.
- Pruebas manuales y automatizadas ejecutadas correctamente.

El proyecto está preparado para la revisión final, la entrega académica y la defensa individual, sujeto a las comprobaciones de GitHub, identificación de integrantes y empaquetado final.
