# Entrega Antigravity — Sprint 06: Notificaciones Push con Firebase (FCM v1)

> 24 de octubre de 2026. Tareas ANTIGRAVITY-17 a ANTIGRAVITY-20 completadas, verificadas con pruebas unitarias y compilación APK exitosa.

---

## 1. Resumen Ejecutivo

En este Sprint 06 se completó la integración integral en el cliente Android (`LaExquisssita`) para el soporte de notificaciones push en segundo plano mediante **Firebase Cloud Messaging (FCM v1)** en sincronía con **Supabase**, cumpliendo rigurosamente con la política de **Cero Emojis** y la identidad visual de marca (*Chocolate Fondant*, *Vanilla Custard*).

---

## 2. Detalle de Implementación

### ANTIGRAVITY-17: Configuración de Gradle y Firebase SDK
- Se agregaron las dependencias y plugins en `gradle/libs.versions.toml`:
  - Plugin `com.google.gms.google-services` v4.4.2.
  - Firebase BoM v33.10.0 y librería `firebase-messaging-ktx`.
- Se configuró el plugin en el `build.gradle.kts` raíz y en `app/build.gradle.kts`.
- Se integró el archivo `google-services.json` verificado en la raíz del módulo `app/`.

### ANTIGRAVITY-18: Permisos y Canal de Notificación del Sistema
- Se declaró `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` en `AndroidManifest.xml`.
- Se implementó `NotificationHelper.kt`:
  - Creación del canal oficial `canal_la_exquisssita` (*"Club La Exquisssita"*) con importancia alta, vibración y soporte para badges.
  - Soporte de renderizado de notificaciones con `NotificationCompat.Builder`, estilo `BigTextStyle` y `PendingIntent` con extras hacia `MainActivity`.
  - Icono vectorial oficial para barra de estado `@drawable/ic_stat_notification` y color temático `@color/chocolate_fondant`.
- Se agregó el launcher de permisos de ejecución en `MainActivity.kt` para dispositivos con Android 13+ (API 33+).

### ANTIGRAVITY-19: Servicio de Mensajería `LaExquisssitaFirebaseMessagingService`
- Implementación del servicio oficial en `core/notifications/LaExquisssitaFirebaseMessagingService.kt`:
  - `onNewToken(token)`: Captura el nuevo token y lo sincroniza de manera asíncrona mediante `AppContainer.pushNotificationRepository`.
  - `onMessageReceived(remoteMessage)`: Extrae título, cuerpo y payload (`data`), mostrando la alerta visual en la bandeja del sistema del dispositivo, asegurando textos libres de emojis.

### ANTIGRAVITY-20: Vinculación con Sesión y Repositorios
- Se crearon los contratos y componentes limpios:
  - `PushTokenStorage.kt`: interfaz para almacenamiento desacoplado de tokens FCM (implementada por `CustomerPreferences`).
  - `PushNotificationRemote.kt` y `SupabasePushNotificationRemote.kt`: invocación segura a las RPCs canónicas de Supabase (`registrar_dispositivo_fcm` y `eliminar_dispositivo_fcm`).
  - `PushNotificationRepositoryImpl.kt`: orquestación que escucha transiciones de sesión (`AuthState.Authenticated`) para registrar el token automáticamente con el modelo del dispositivo, y desvincularlo al cerrar sesión.
- Se corrigió el ruteo de `signOut` y `deleteAccount` en `CustomerViewModel.kt` para garantizar que la desvinculación ocurra antes de invalidar el JWT local.

---

## 4. Estado de Despliegue en Producción

- **Edge Function:** `send-push-notification` desplegada exitosamente en Supabase (`https://gcszsxeywywsrgnnnljr.supabase.co/functions/v1/send-push-notification`).
- **Migración Remota:** `20261024201145_customer_push_notifications.sql` aplicada con `npx supabase db push --linked --yes`.
- **Sincronización:** 34/34 migraciones idénticas entre local y remoto.
- **Supabase Vault:** Secretos `fcm_push_function_url` y `fcm_push_webhook_secret` configurados para invocar la Edge Function desde los triggers de base de datos.

- **Pruebas Unitarias (`PushNotificationRepositoryTest.kt`):**
  - Almacenamiento local del token en `storage`.
  - Registro exitoso en backend remoto cuando el socio está autenticado.
  - Omisión de llamada remota cuando el usuario navega como invitado (guest).
  - Consulta automática al proveedor de tokens en caso de valor nulo.
  - Desvinculación de token en logout.
  - Disparo automático de registro ante la transición de estado de sesión.
  - **Resultado:** **31 pruebas unitarias completadas al 100% (BUILD SUCCESSFUL en 14s)**.
- **Compilación de Artefacto APK (`:app:assembleDebug`):**
  - Procesamiento exitoso de `google-services.json`.
  - Generación del APK de depuración sin errores (**BUILD SUCCESSFUL en 1m 20s**).
