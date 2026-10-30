# Entrega Codex — integración del Sprint 01

> 18 de octubre de 2026. Datos, contratos y ViewModel implementados. Pantallas conservadas para Antigravity.

## Punto de entrada para Antigravity

Obtener una única instancia de `CustomerViewModel` con alcance de Activity y compartirla entre las pestañas y Configuración:

```kotlin
val app = LocalContext.current.applicationContext as LaExquisssitaApp
val customer: CustomerViewModel = viewModel(factory = app.container.customerViewModelFactory)
val session by customer.authState.collectAsStateWithLifecycle()
val card by customer.state.collectAsStateWithLifecycle()
val brightness by customer.autoMaxBrightness.collectAsStateWithLifecycle()
```

Crear el ViewModel en el shell de Activity, no una instancia por destino. Pasar estados y callbacks a las pantallas. Para abrir Credential Manager, pasar la Activity actual a `customer.signIn(activity)`; no usar Application como contexto visual.

## Contratos disponibles

- `AuthState.Initializing`: restauración de sesión; no simular un usuario autenticado.
- `Guest`: acceso a promociones y botones de Google en las secciones privadas.
- `LoadingProfile(userId)`: Google autenticó, el perfil aún está cargando.
- `ProfileError(userId, message)`: ofrecer `customer.refresh()`; no reiniciar automáticamente Google.
- `Authenticated(userId, profile, email, isOffline)`: perfil real y saldo/visitas. `isOffline` indica datos almacenados aún no confirmados con el servidor.
- `customer.state`: `qrToken`, `loadingCard`, `busy`, `showPhoneOnboarding`, `message` y `userId`.
- `customer.signIn(activity)`, `refresh()`, `loadCard()`, `saveProfile(name, phone)`, `skipPhone()`, `signOut()`, `setAutoMaxBrightness(enabled)`, `clearMessage()`.

El teléfono vacío se envía como null; válido si está ausente o contiene exactamente 10 dígitos. El nombre se recorta y admite 1–160 caracteres. Omitir teléfono oculta el diálogo durante la sesión actual del ViewModel; no supone consentimiento para campañas.

La UI genera la imagen a partir de `qrToken` y conecta GallerySaver. El ViewModel no guarda bitmaps ni retiene Activities. Al consumir `message`, llamar a `clearMessage()`. La cancelación del selector de Google no muestra error.

No conectar ningún botón a `rotate_customer_qr_token`; se retiró la rotación de `LoyaltyRepository`. `refresh()` actualiza datos sin reemitir la tarjeta. Google Wallet sigue fuera de este sprint.

## Datos y seguridad

- `AuthRepository.state` refleja sesión, carga de perfil y recuperación offline. Fallar al cargar el perfil permite reintento sin perder la sesión.
- `LoyaltyRepository` recupera QR con `get_or_create_customer_qr_token`; caché por usuario, validación del cliente devuelto y exclusión mutua de solicitudes.
- Perfil y QR usan AES-GCM con clave Android Keystore; sesión también cifrada. Respaldo/transferencia excluyen estos archivos. Se retira el almacenamiento previo en claro de esta base pre-release.
- Cerrar sesión limpia inmediatamente estado/datos locales, incluso si falla la llamada de red. Nunca revoca el QR remoto.
- Respuestas de otra sesión se descartan. La cancelación de coroutines no se convierte en error de negocio.
- Recompensas e historial usan columnas explícitas autorizadas, filtro de cliente para historial y límites de 100 recompensas / 50 movimientos recientes. Paginación ampliada queda para la siguiente fase.
- Preferencia de brillo persistente, separada de la sesión.

## Configuración

`local.properties.example` lista las tres entradas públicas. Se admiten también variables de entorno o propiedades Gradle (prioridad: entorno, Gradle, archivo local).

La URL y clave pública se reutilizaron desde la configuración local del Manager. El Web Client ID fue proporcionado y agregado únicamente a `local.properties` (ignorado por Git); se compiló con ese valor. El secreto OAuth permanece únicamente en el proveedor/backend. Probar Google en dispositivo con paquete y firma Android registrados.

Kotlin se alineó a 2.4.0 para las dependencias Supabase 3.8.0; se añadió desugaring para minSdk 24. Se mantiene AppContainer, sin migración a Hilt.

## Backend y validación

Ver [backend preparado](../../supabase/README.md) y [contrato](../BACKEND-CONTRACT-CLIENT.md). El catálogo remoto confirmó ausencia de RPC de clientes, presencia de Vault 0.3.1/pgcrypto 1.3 y permiso SELECT de anon en promociones. Esto no valida aún columnas ni filtro de vigencia del feed público.

Compilación APK debug, pruebas unitarias y lint ejecutados. Las 11 pruebas nuevas de datos cubren recuperación offline, reintento de perfil, reinstalación simulada, concurrencia local, salida con fallo de red, respuesta tardía, cliente incorrecto, cambio de cuenta, cancelación y validación de campos. No sustituyen la prueba SQL ni el acceso Google en dispositivo.

Pendientes: prueba Google en dispositivo y escaneo real con Manager; registrar esta migración en la cadena local del repositorio Manager y repetir una base limpia antes de desplegar. La base remota no tiene las RPC Release 03. No se modificó ni desplegó el backend remoto.

Para reproducir Gradle en este equipo fue necesario JAVA_HOME al JBR de Android Studio y `JAVA_TOOL_OPTIONS=-Djdk.net.unixdomain.tmpdir=C:\Windows\Temp` para evitar un fallo de sockets temporales. Es una variable de proceso; no se cambió la configuración global.


SQL local: pgTAP 14/14 aprobado; prueba con dos conexiones simultáneas aprobada y fixture eliminado. Advisors completados: seis advertencias por políticas SELECT superpuestas ya presentes en Release 03; ninguna corresponde al nuevo objeto de recuperación QR. Ver [resultado detallado](../../supabase/README.md).

