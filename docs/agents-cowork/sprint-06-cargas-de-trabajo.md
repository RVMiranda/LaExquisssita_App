# Sprint 06 — Cargas de Trabajo: Notificaciones Push con Firebase (FCM) y Supabase

> **Fecha:** 23 de octubre de 2026  
> **Alcance:** Integración integral de Firebase Cloud Messaging (FCM v1) con Supabase y la app Android nativa (*La Exquisssita*) para alertas en segundo plano (puntos acumulados en caja, visitas registradas y avisos de promociones), respetando el diseño de marca y la política de cero emojis.

---

## 1. Arquitectura de Notificaciones Push (FCM v1 + Supabase)

```
┌─────────────────────────┐          ┌──────────────────────────┐          ┌─────────────────────────┐
│     App Android         │          │     Supabase Backend     │          │    Google Firebase      │
│   (La Exquisssita)      │          │  (Postgres + Edge Func)  │          │      (FCM v1 API)       │
└───────────┬─────────────┘          └────────────┬─────────────┘          └────────────┬────────────┘
            │                                     │                                     │
            │ 1. Obtiene FCM Token de Google      │                                     │
            │    y registra en Supabase           │                                     │
            ├────────────────────────────────────>│ Guarda en public.dispositivos_fcm   │
            │                                     │                                     │
            │                     2. Evento: Caja agrega puntos a cliente               │
            │                        Trigger/Webhook activa Edge Function               │
            │                                     ├────────────────────────────────────>│
            │                                     │ 3. Edge Func intercambia            │
            │                                     │    Service Account por OAuth token  │
            │                                     │    y envía POST a FCM v1 API        │
            │                                     │<────────────────────────────────────┤
            │                                     │                                     │
            │ 4. FCM entrega alerta al dispositivo│                                     │
            │<──────────────────────────────────────────────────────────────────────────┤
            │                                                                           │
            │ 5. LaExquisssitaFirebaseMessagingService muestra notificación             │
            │    en bandeja del sistema con estilo de marca                             │
```

---

## 2. Asignación de Tareas por Agente

### Responsabilidad de Codex (Capa de Datos y Backend Supabase)

- [x] **[CODEX-22] Tabla de Dispositivos y RLS en Supabase:**
  - Crear migración en `ExquisssitaAppManager` para la tabla `public.dispositivos_fcm`:
    - `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
    - `cliente_id UUID NOT NULL REFERENCES public.clientes(id) ON DELETE CASCADE`
    - `fcm_token TEXT NOT NULL UNIQUE`
    - `plataforma TEXT NOT NULL DEFAULT 'android'`
    - `modelo_dispositivo TEXT`
    - `actualizado_el TIMESTAMPTZ NOT NULL DEFAULT now()`
    - `creado_el TIMESTAMPTZ NOT NULL DEFAULT now()`
  - Habilitar RLS: cada cliente solo puede consultar, insertar, actualizar y eliminar sus propios dispositivos mediante la relación `clientes.user_id = auth.uid()` (el UUID de `cliente_id` no es el UUID de Auth).
  - RPC segura `registrar_dispositivo_fcm(p_token text, p_modelo text default null)` para upsert idempotente.
  - RPC segura `eliminar_dispositivo_fcm(p_token text)` para desconexión limpia al cerrar sesión o borrar cuenta.

- [x] **[CODEX-23] Edge Function para Despacho FCM v1 (`send-push-notification`):**
  - Implementar la función Deno en `supabase/functions/send-push-notification/index.ts`.
  - Leer el secreto `FCM_SERVICE_ACCOUNT` (JSON de la clave de servicio descargada).
  - Implementar el intercambio de credenciales JWT hacia `https://oauth2.googleapis.com/token` con scope `https://www.googleapis.com/auth/firebase.messaging`.
  - Construir el payload FCM v1 (`message.token`, `message.notification`, `message.data`, `message.android`).
  - Enviar petición POST a `https://fcm.googleapis.com/v1/projects/laexquisssita-app/messages:send`.
  - Manejo de tokens inválidos o expirados (eliminar token obsoleto de la base si FCM responde `UNREGISTERED`).

- [x] **[CODEX-24] Disparadores de Eventos (Triggers / Database Webhooks):**
  - Trigger en `puntos_movimientos` al insertar un movimiento `visita` o un ajuste positivo; el nombre `visita_acumulada` no existe en el `CHECK` actual del ledger. Los títulos y cuerpos cumplen la política de cero emojis.
  - Trigger después de insertar una promoción activa o cuando cambia de inactiva a activa, para difundirla a los dispositivos registrados.

La implementación de Codex y el estado de validación están en [sprint-06-entrega-codex.md](sprint-06-entrega-codex.md). La migración se mantiene local hasta ejecutar pgTAP y configurar los secretos de FCM y del webhook; no se han desplegado migraciones ni funciones.

---

### Responsabilidad de Antigravity (App Android `LaExquisssita`)

- [ ] **[ANTIGRAVITY-17] Configuración de Gradle y Dependencias de Firebase:**
  - Agregar el plugin de Google Services (`com.google.gms.google-services`) en `libs.versions.toml`, `build.gradle.kts` raíz y `app/build.gradle.kts`.
  - Agregar Firebase BoM y dependencia `com.google.firebase:firebase-messaging-ktx`.
  - Verificar compatibilidad de compilación (`assembleDebug`).

- [ ] **[ANTIGRAVITY-18] Permisos de Notificación y Canal en Android:**
  - Declarar permiso `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` en `AndroidManifest.xml`.
  - Crear el canal de notificación oficial del sistema (`canal_la_exquisssita`) con importancia alta y sonido por defecto.
  - Implementar solicitud de permisos en Compose para Android 13+ (API 33+) con explicación amigable.

- [ ] **[ANTIGRAVITY-19] Servicio de Mensajería `LaExquisssitaFirebaseMessagingService`:**
  - Manejar `onNewToken`: almacenar el token localmente y sincronizarlo con Supabase cuando haya un socio autenticado.
  - Manejar `onMessageReceived`: construir y desplegar la notificación en la bandeja del sistema utilizando los colores de marca (`ChocolateFondant`, `VanillaCustard`), icono de notificación y `PendingIntent` a `MainActivity`.
  - Respetar estrictamente la política de Cero Emojis en los títulos y cuerpos generados.

- [ ] **[ANTIGRAVITY-20] Integración en Ciclo de Vida de Sesión:**
  - Al iniciar sesión con éxito (Google o Correo), obtener el token actual de FCM con `FirebaseMessaging.getInstance().token` y registrarlo en Supabase.
  - Al cerrar sesión o ejecutar baja de cuenta, invocar la desvinculación del token en Supabase.
  - Pruebas unitarias de repositorio y verificación visual en emulador Android.
