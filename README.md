# Proyect-Plataformas — LocalHands

Aplicación Android en Kotlin y Jetpack Compose. Esta versión añade la parte técnica de Jair: guardar y recuperar un borrador de solicitud con Room, a través del Repository existente.

Se conservan el módulo `app`, los paquetes, las rutas, las pantallas y la inyección manual. No se movieron ni renombraron archivos del proyecto original. Las nuevas clases se añadieron a los paquetes que ya existían.

## Qué hace el borrador

1. Abre un prestador y entra en **Solicitar servicio**.
2. Escribe la descripción y, si quieres, elige zona, fecha y horario. Puede estar incompleto.
3. Toca **Guardar borrador** y espera **Borrador guardado en este teléfono. Puedes cerrar la aplicación.**
4. Al volver a **Solicitar servicio** del mismo prestador, se recuperan los campos guardados automáticamente.
5. Si cambias algo, vuelve a tocar **Guardar borrador**. No hay guardado automático por cada tecla.
6. Al enviar correctamente la solicitud, se elimina su borrador. Si el envío falla, se conserva el último borrador guardado.

Hay un borrador por prestador en este dispositivo. Guardar de nuevo reemplaza solo el de ese prestador. Guardar un borrador no crea un pedido ni escribe en Firestore.

## Implementación de Jair

| Archivo / paquete existente | Responsabilidad |
| --- | --- |
| `data/model/RequestDraft.kt` | Modelo de dominio con campos opcionales para formularios incompletos. |
| `data/repository/RequestDraftEntity.kt` | Tabla `request_drafts`; `providerId` es la clave primaria. |
| `data/repository/RequestDraftDao.kt` | Consultar, insertar/reemplazar y eliminar mediante funciones `suspend`. |
| `data/repository/LocalHandsDatabase.kt` | Base Room persistente, versión 1. |
| `data/repository/LocalHandsRepository.kt` | Contrato de acceso a borradores y datos remotos. |
| `data/repository/FirestoreLocalHandsRepository.kt` | Implementa ambas fuentes y convierte Entity ↔ dominio dentro del Repository. |
| `di/AppContainer.kt` | Crea una sola base `localhands.db` por contenedor de Application e inyecta su DAO. |
| `LocalHandsApplication.kt` | Proporciona el contexto de aplicación al contenedor. |
| `ui/screens/request/RequestServiceViewModel.kt` | Recuperación inicial, guardado, errores y limpieza después del envío, usando `viewModelScope`. |
| `ui/state/FormStates.kt` | Estado inmutable del formulario y de las operaciones del borrador. |
| `ui/screens/request/RequestServiceScreen.kt` | Botón, estados visibles y campos deshabilitados durante las operaciones. |
| `navigation/LocalHandsNavHost.kt` | Pasa el nuevo callback y el aviso de caché al formulario; mantiene las rutas. |
| `gradle/libs.versions.toml` y archivos Gradle | Room 2.8.5 y KSP 2.3.10, siguiendo el catálogo de versiones existente. |

### Métodos locales del Repository

```kotlin
suspend fun getRequestDraft(providerId: String): RequestDraft?
suspend fun saveRequestDraft(draft: RequestDraft)
suspend fun deleteRequestDraft(providerId: String)
```

La lectura devuelve `null` cuando no hay borrador. Estos métodos no llaman a `FirebaseSession`, a Firestore ni a sus escritores. El DAO ejecuta las consultas de forma asíncrona con `suspend`; no se habilitan consultas en el hilo principal.

| Columna de Room | Tipo / significado |
| --- | --- |
| `providerId` | `String`, clave primaria, identificador del prestador. |
| `description` | `String`, conserva el texto exacto, incluidos espacios y tildes. |
| `zone` | `String?`, zona elegida o `null`. |
| `date` | `String?`, fecha ISO `yyyy-MM-dd` o `null`. |
| `timeSlot` | `String?`, nombre del enum `MORNING` / `AFTERNOON` o `null`. |

El Repository convierte fecha y horario a `LocalDate?` y `TimeSlot?`. No se persisten errores de pantalla, cargas ni la marca `submitted`. Se mantienen las validaciones existentes al enviar; una fecha recuperada que ya pasó debe corregirse.

Se distinguen los estados de recuperar, guardar y enviar. No se puede sobrescribir el formulario mientras se recupera el borrador, ni enviar mientras se guarda. La confirmación de guardado aparece después de terminar la escritura. Los errores conservan los campos y no cierran la aplicación. La cancelación de una corutina se propaga.

La limpieza del borrador se hace aparte del envío: si Firestore aceptó el envío pero falla la eliminación local, se muestra una advertencia y se mantiene la solicitud como enviada. No se propone repetir el envío. También se elimina cuando la implementación existente comunica `RequestConfirmationPendingException`, porque ya creó una escritura pendiente.

### Decisiones y límites

- La persistencia del borrador no necesita Internet. Para llegar al formulario sin conexión mediante las pantallas existentes, primero debe haberse cargado el prestador y establecido la sesión anónima; la navegación utiliza la caché de Firestore. No se añadió una pantalla independiente de borradores.
- La primera apertura de la app sin sesión ni datos precargados no permite explorar prestadores. Esto no borra un borrador local.
- Cerrar o forzar la detención conserva los datos ya confirmados. Desinstalar la app o borrar sus datos no es una prueba de conservación.
- Las escrituras nuevas de solicitudes siguen verificando al prestador con `Source.SERVER`. Por ello no se promete poder enviar desde el inicio en modo avión. Room permite conservar el formulario y enviarlo al volver la conexión.
- Las calificaciones existentes usan una transacción de Firestore y necesitan conexión. No se añadió una cola local de calificaciones.
- La base usa `exportSchema = false` porque esta es la primera versión y se mantiene la estructura de carpetas. Antes de cambiar su esquema, exportar/versionar esquemas e implementar y probar la migración. No se usa `fallbackToDestructiveMigration`.
- Los borradores son locales por prestador, no por cuenta. La app todavía no incluye cambio de usuario con perfil propio.

## Compilación y pruebas en Android Studio

**Estado de esta entrega de código:** se hizo revisión estática y comparación con el ZIP original. La compilación y las pruebas Android quedan pendientes de ejecución. En el entorno de edición Java no puede iniciar (`libjli.so` ausente) y no hay SDK/emulador Android. No se afirma que los tests hayan pasado ni que se haya probado el cierre real del proceso.

1. Abre la carpeta que contiene `settings.gradle.kts`, no `app` por separado.
2. Usa el JDK 21 configurado para Gradle en el proyecto y deja que Android Studio sincronice.
3. Mantén las versiones originales de AGP, Kotlin, Gradle y SDK. El proyecto solicita `compileSdk` 37, minor API 1; instala la plataforma correspondiente y las herramientas que pida Android Studio.
4. En la terminal de Android Studio, ejecuta:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

En macOS/Linux: `bash gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.

Para probar solo Room en un dispositivo/emulador, incluso sin red:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.proyect_plataformas.RequestDraftRepositoryTest"
```

No ejecutes toda la instrumentación esperando que sea independiente de Internet: los tests heredados `FirestoreRepositoryIntegrationTest` necesitan Firebase, datos existentes y, para calificar, cambiar manualmente una solicitud a `TO_RATE` durante la prueba. Las reglas del archivo `firestore.rules.txt` deben estar desplegadas y la autenticación anónima habilitada para el flujo remoto.

### Pruebas añadidas

- `RequestDraftRepositoryTest`: cinco casos sobre un archivo real de Room, usando el Repository: persistencia tras cerrar/recrear base y Repository, campos nulos, reemplazo por prestador, eliminación aislada e inexistencia. No acceden a la red ni escriben en Firestore.
- `RequestServiceViewModelTest`: diez casos nuevos para recuperación con proveedor en caché, formulario incompleto, aislamiento por prestador, errores de lectura/guardado, operaciones lentas, limpieza tras envío/reintento, envío pendiente, error de limpieza y fecha vencida. Utilizan el fake existente, ampliado con borradores.
- Se conservaron las pruebas anteriores del flujo.

Cerrar y reabrir una base en un test no demuestra por sí solo el cierre completo del proceso. Completar también la prueba manual siguiente.

### Evidencia manual obligatoria: cerrar y reabrir sin conexión

| Paso | Acción | Resultado esperado |
| --- | --- | --- |
| 1 | Con conexión, abrir la app, buscar un prestador disponible y entrar en Solicitar servicio. | Prestador visible y sesión establecida; se llena la caché. |
| 2 | Escribir un texto identificable, elegir zona, fecha futura y horario. Tocar Guardar borrador y esperar su confirmación. | Escritura local terminada. |
| 3 | Activar modo avión y comprobar que Wi-Fi también está apagado. | Sin conexión efectiva. |
| 4 | Ajustes de Android → Aplicaciones → LocalHands → Forzar detención. | Proceso detenido; no basta con rotar la pantalla o pulsar Inicio. |
| 5 | Abrir desde el icono, volver al mismo prestador y a Solicitar servicio. | Aparecen los mismos cuatro campos y el aviso de borrador recuperado. |
| 6 | Modificar un campo, guardar de nuevo, forzar detención y reabrir todavía sin conexión. | Se recupera la última versión confirmada. |
| 7 | Abrir otro prestador y su formulario. | No se mezclan los borradores. |
| 8 | Volver a tener conexión, enviar el primer formulario y consultar Mis pedidos. Reabrir su formulario. | Pedido visible; borrador eliminado. |

También puede detenerse con `adb shell am force-stop com.example.proyect_plataformas`. No usar `pm clear`, desinstalar ni borrar los datos.

Registrar aquí el resultado real:

| Dato | Registro |
| --- | --- |
| Fecha y responsable | PENDIENTE |
| Dispositivo, Android y versión de la app | PENDIENTE |
| SHA del commit probado | PENDIENTE |
| Texto, zona, fecha y horario antes/después | PENDIENTE |
| Modo avión y Wi-Fi apagado comprobados | PENDIENTE |
| Cierre completo y recuperación | PENDIENTE: aprobado / falló, con detalle |
| Evidencia | PENDIENTE: enlace o minuto del video |

## Revisión de convenciones y observaciones

Revisión estática de las capas de datos, inyección, navegación, ViewModels, estados y usos de listas del proyecto. Falta ejecutar lint y revisar el resultado en Android Studio.

| Convención | Revisión / decisión |
| --- | --- |
| Nombres | Se mantiene inglés en clases/funciones: PascalCase para clases y composables; camelCase para funciones y propiedades. Mensajes y comentarios siguen el español predominante. |
| Estructura | Mismos módulos y paquetes. Entidad, DAO y base se añadieron en `data.repository`, junto con los auxiliares de persistencia existentes. Modelo de dominio en `data.model`. |
| Acceso a datos | Pantallas y ViewModels no importan Room ni Firebase. Solo el Repository usa el DAO; el contenedor lo construye para inyección. |
| Estado | `MutableStateFlow` privado; se expone `StateFlow` con datos inmutables y `copy`. Las pantallas reciben valores/callbacks. |
| Corutinas | Operaciones del formulario en `viewModelScope`; DAO `suspend`. Sin `GlobalScope`, `allowMainThreadQueries` ni base en memoria en producción. |
| Listas | Inicio, Resultados, Mis pedidos y reseñas del perfil ya usan claves por identificador. |
| Navegación | Argumentos mediante `SavedStateHandle`, rutas y botón Atrás existentes. No se agregaron destinos ni se cambiaron los identificadores. |
| Formularios | Se conservan las validaciones existentes; guardar borrador permite campos incompletos. Se deshabilitan entradas y acciones mientras se lee/guarda/envía. |
| Textos de UI | Se mantiene el estilo existente de textos en composables/estados; migrarlos todos a recursos sería un cambio general. Confirmar si el docente exige internacionalización. |
| Datos de ejemplo | `MockLocalHandsData` sigue en previews/pruebas y en `currentUser()`: el perfil de vecino es de ejemplo, como documenta el contrato original. Prestadores/pedidos de ejecución provienen de Firestore. No presentar autenticación con perfil real como terminada. |
| Modelo de reseñas heredado | Las reseñas se guardan en `providers.reviews` y se actualiza `serviceRequests`; no hay una colección independiente de reseñas. Si el requisito de James exige tres colecciones separadas, sigue pendiente corregirlo con James o justificarlo ante el docente. No se alteró ese modelo en la parte de Jair. |
| Cuenta / contadores | El ViewModel heredado de Cuenta muestra cero ante fallos de lectura; no expone un estado visible de error. Documentar con Iván si el docente pide ese estado también en Cuenta. |

**Las observaciones textuales del docente y la rúbrica completa no estaban incluidas.** La tabla anterior contiene hallazgos de revisión, no comentarios atribuidos al docente ni una aprobación suya. Completar cada observación real sin darla por resuelta por anticipado:

| Observación exacta del docente | Corrección o justificación | Archivo / evidencia | Estado |
| --- | --- | --- | --- |
| PENDIENTE: copiar observación real | PENDIENTE | PENDIENTE | Pendiente |

## Task test con una persona externa real

**Estado: preparado, no realizado.** Elegir una persona que no sea integrante del equipo y no haya implementado la aplicación. Acordar una sesión de 10–15 minutos; explicar que se evalúa la aplicación y pedir permiso antes de grabar voz o imagen. Puede identificarse con un alias.

Preparación del moderador: app instalada, prestador disponible cargado con conexión y fecha válida para el servicio. No explicar la ubicación de botones ni guiar los pasos durante la tarea. Registrar las ayudas si se vuelven necesarias. La prueba técnica de modo avión puede complementarse después de la tarea, indicando quién la hizo.

Leer a la persona este enunciado:

> Imagina que tienes un problema eléctrico en la cocina y quieres contactar a alguien que trabaje en tu zona. Busca un profesional, revisa su perfil y prepara una solicitud para una fecha y horario que te sirvan. Antes de enviarla, guarda lo que escribiste para continuar después. Cierra por completo la aplicación, ábrela de nuevo y continúa con esa solicitud. Envíala y muéstrame dónde revisarías su estado.

Criterios de éxito: encuentra un prestador adecuado, comprende el perfil, guarda el borrador, recupera sus datos después de reabrir y localiza la solicitud enviada en Mis pedidos. Registrar tiempo, errores, retrocesos y ayudas, además del resultado. No convertir expectativas en resultados observados.

| Campo | Registro que debe completarse durante/después de la sesión |
| --- | --- |
| Participante externo y relación con el equipo | PENDIENTE: alias y por qué es externo |
| Fecha, dispositivo y versión / SHA | PENDIENTE |
| Tarea aplicada | Enunciado anterior; anotar cualquier modificación |
| Inicio, fin y duración | PENDIENTE |
| Resultado | PENDIENTE: completó sin ayuda / con ayuda / no completó |
| Dificultades observadas | PENDIENTE: comportamiento concreto y paso; evitar suposiciones |
| Ayudas del moderador | PENDIENTE: qué se explicó y cuándo |
| Comentarios de la persona | PENDIENTE: palabras reales, si las hubo |
| Cambios necesarios | PENDIENTE: cambio, motivo, prioridad y responsable |
| Cambios aplicados y verificación | PENDIENTE: commit/evidencia, o razón para posponer |

## Alcance para consolidar con el equipo

**Propuesta basada en el código; falta acuerdo explícito de James, Iván y Jair.** Sustituir los identificadores siguientes por los de las historias reales si existen.

| Historia | Alcance propuesto | Responsable / estado de evidencia |
| --- | --- | --- |
| Buscar y filtrar prestadores de Firestore | Dentro | James + Iván; validar en dispositivo. |
| Consultar perfil | Dentro | James + Iván; validar con datos remotos/caché. |
| Enviar solicitud y encontrarla en Mis pedidos | Dentro | James + Iván; requiere verificación de servidor para crear. |
| Calificar una solicitud `TO_RATE` y actualizar perfil | Dentro | James + Iván; preparar solicitud real para probar. |
| Guardar y recuperar borrador local por prestador | Dentro | Jair; implementación y tests añadidos, ejecución pendiente. |
| Carga, error, vacío y lecturas en caché de pantallas principales | Dentro | Iván; validar los casos, incluida la observación de Cuenta. |
| Login con perfil propio / cambio de cuentas | Fuera | Sesión anónima y vecino de ejemplo actuales. |
| Panel del prestador y cambio de pedido a terminado | Fuera | `TO_RATE` se prepara administrativamente para la demostración. |
| Pagos, chat, notificaciones y geolocalización | Fuera | No forman parte del flujo implementado. |
| Envío nuevo desde modo avión sin comprobación de servidor | Fuera | Se conserva el borrador; no se promete envío offline. |
| Sincronizar borradores entre dispositivos / lista general de borradores | Fuera | Room es local y la recuperación se hace en el formulario del mismo prestador. |

Registrar la fecha y participantes del acuerdo real antes del PR final: **PENDIENTE**. Resolver también si el requisito de reseñas admite el modelo embebido actual.

## Video continuo de máximo tres minutos

**Estado: guion preparado; grabación pendiente.** Este guion cubre los requisitos descritos en la consigna recibida. Compararlo con la rúbrica completa para no omitir evidencias adicionales. Grabar una sola toma, sin cortes, de hasta 3:00; objetivo 2:55.

Preparar antes: tamaño de ventanas legible, app instalada, prestador en caché, una solicitud propia distinta ya en `TO_RATE`, Firestore Console abierta, resultado real del task test, observaciones y alcance acordado. El código demostrado debe corresponder al SHA declarado; no modificar código durante la grabación.

| Tiempo | Evidencia a mostrar |
| --- | --- |
| 0:00–0:15 | LocalHands, integrantes, rama `entrega-4` y SHA del commit que se está probando. |
| 0:15–0:50 | Buscar prestador, perfil, crear una solicitud, verla en Mis pedidos y mostrar el documento correspondiente en Firestore. |
| 0:50–1:15 | Calificar la solicitud `TO_RATE` preparada; mostrar la reseña y actualización del perfil en app/Firestore. |
| 1:15–2:05 | En otro formulario, llenar campos y guardar borrador; activar modo avión con Wi-Fi apagado, forzar detención y reabrir; recuperar los mismos campos en el mismo prestador. |
| 2:05–2:25 | Mostrar Entity, DAO, Database, métodos del Repository, inyección y StateFlow del ViewModel. Si está listo, mostrar `request_drafts` en Database Inspector. |
| 2:25–2:40 | Registro del task test real: tarea, resultado, dificultad y cambio aplicado/pendiente. |
| 2:40–2:55 | Alcance acordado, observaciones atendidas/justificadas y declaración de IA; PR abierto de `entrega-4` hacia `main`. |

Al subir la toma, comprobar duración, continuidad, audio, legibilidad y acceso del docente al enlace. No marcar video entregado hasta tener el archivo/enlace real.

## PR de entrega-4 hacia main

El ZIP recibido no contiene historia `.git`, SHA ni URL verificable del repositorio. No se creó un PR ni se inventó un SHA. Aplicar los cambios en el clon del equipo preservando su historial; no inicializar otro repositorio para sustituirlo.

1. Abrir el clon real del equipo, actualizarlo según sus acuerdos y situarse en la rama **entrega-4**. Si ya existe, usarla; crearla solo si aún no existe.
2. Copiar los archivos modificados/nuevos de esta versión y revisar el diff. No reemplazar `.git`, ni copiar configuraciones locales de otra máquina.
3. Ejecutar compilación, tests y lint; completar la prueba offline, task test, correcciones y acuerdo de alcance. Guardar los resultados reales.
4. Hacer el commit de implementación/evidencias y obtener su SHA con `git rev-parse HEAD`. Usar ese commit en el video. Si cambia el código después, actualizar SHA y evidencias.
5. Publicar la rama en el remoto y abrir el PR con **base: main** y **compare: entrega-4**. Mantenerlo abierto, sin auto-merge ni fusión. Si ya existe el PR del equipo, actualizar ese mismo.
6. Pegar la descripción siguiente y sustituir todos los campos pendientes. Grabar/subir la toma y editar la descripción con su enlace real. Verificar acceso.
7. Copiar la URL real del PR abierto y entregarla en Canvas. Comprobar después que siga abierto y sin fusionar.

### Título sugerido

`Entrega 4: flujo con Firestore y borrador de solicitud con Room`

### Descripción para completar y pegar en el PR

```markdown
La aplicación permite buscar un prestador, consultar su perfil, crear una solicitud,
verla en Mis pedidos y calificar un servicio preparado como TO_RATE. Se añade un
borrador local con Room, recuperado al volver al formulario del mismo prestador.

## Integrantes y contribuciones
- James [completar nombre y usuario]: configuración y Repository de Firestore.
- Iván [completar nombre y usuario]: pantallas, ViewModels, navegación y flujo.
- Jair [completar nombre y usuario]: Room, revisión y consolidación de evidencias.

## Versión y evidencia
- Base: main. Rama de entrega: entrega-4.
- SHA completo del commit probado y mostrado: PENDIENTE.
- Video continuo (máximo 3:00): PENDIENTE enlace real y duración.
- Dispositivo / Android / fecha: PENDIENTE.
- Compilación, pruebas y lint: PENDIENTE resultados reales y evidencia.
- Recuperación de borrador tras forzar detención en modo avión: PENDIENTE resultado.

## Task test real
- Participante externo (alias y relación): PENDIENTE.
- Tarea y fecha: PENDIENTE.
- Resultado, tiempo y ayudas: PENDIENTE.
- Dificultades observadas: PENDIENTE.
- Cambios necesarios/aplicados y evidencia: PENDIENTE.

## Alcance acordado
- Fecha y participantes del acuerdo: PENDIENTE.
- Historias dentro: PENDIENTE acuerdo/IDs; ver propuesta en README.
- Historias fuera: PENDIENTE acuerdo/IDs; ver propuesta en README.

## Observaciones del docente
- PENDIENTE: observación textual → corrección o justificación → archivo/evidencia.
- Revisar requisito de colección de reseñas: el modelo actual usa providers.reviews.
- Confirmar tratamiento de carga/error en Cuenta y demás observaciones reales.

## Uso de IA
Se utilizó ChatGPT/Codex para revisar el proyecto adjunto, proponer e implementar
la persistencia del borrador con Room, ampliar pruebas y preparar documentación
para task test, video y PR, respetando la estructura y estilo existentes.
La compilación y los tests Android no pudieron ejecutarse en el entorno de IA.
- Revisión y validación realizadas por el equipo: PENDIENTE detallar las reales.
- Otras herramientas/usos de IA del equipo: PENDIENTE declarar si existieron.
El task test, sus resultados, el video y las observaciones del docente deben
corresponder a evidencias reales; no fueron realizados ni inventados por la IA.

El PR se deja abierto, sin fusionar. URL entregada en Canvas: PENDIENTE confirmar.
```

## Fuentes técnicas consultadas

- [Room 2.x y sus dependencias](https://developer.android.com/jetpack/androidx/releases/room)
- [Consultas asíncronas de Room](https://developer.android.com/training/data-storage/room/async-queries)
- [KSP y requisitos](https://kotlinlang.org/docs/ksp-overview.html)
- [Kotlin integrado en AGP 9](https://developer.android.com/build/migrate-to-built-in-kotlin)
