# Entrega Antigravity — Sprint 05: Baja de Cuenta, Marco Legal y Contacto Directo

> 23 de octubre de 2026. Tareas ANTIGRAVITY-13 a ANTIGRAVITY-16 completadas, migración remota sincronizada y verificado en emulador.

---

## 1. Resumen Ejecutivo

En este Sprint 05 se completó la integración integral de la baja de cuenta (soft delete con anonimización y lista de exclusión definitiva), la vinculación interactiva del marco legal en Configuración, y la incorporación del canal directo de atención vía WhatsApp en el Inicio, cumpliendo con la estricta política de diseño de marca y cero emojis.

Asimismo, se verificó la entrega de la capa de datos de Codex (CODEX-18 a CODEX-21) y se ejecutó exitosamente el despliegue de la migración `20261023030503_customer_account_deletion_privacy.sql` hacia el proyecto remoto de Supabase mediante `npx supabase db push --linked --yes`.

---

## 2. Detalle de Implementación

### ANTIGRAVITY-13: Banner de WhatsApp en Inicio (`HomeScreen.kt`)
- Se retiró la sección redundante de promociones del Inicio (ahora alojada con mayor amplitud en su propia pestaña del menú inferior).
- Se diseñó el componente `WhatsAppContactBanner`:
  - Contenedor plano con fondo `VanillaCustard` y borde sutil `BrandyBrown`.
  - Icono vectorial oficial de chat / mensajería.
  - Título: *"¿Tienes dudas o deseas hacer un pedido especial?"* y subtítulo de atención inmediata.
  - Botón de acción con estilo de marca (`ChocolateFondant`) que abre un `Intent.ACTION_VIEW` apuntando a `https://wa.me/529196732035` con un texto predeterminado de consulta/pedido.

### ANTIGRAVITY-14: Marco Legal Interactivo en Configuración (`SettingsScreen.kt`)
- Las filas de **"Aviso de Privacidad"** y **"Términos del Club de Lealtad"** ahora responden al clic del usuario.
- Despliegan el modal interactivo `LegalDialog` que renderiza el texto íntegro, formateado y con scroll, permitiendo consultar los términos legales en cualquier momento sin requerir conexión externa.

### ANTIGRAVITY-15: Flujo y Diálogo de Eliminación de Cuenta (`SettingsScreen.kt` + `CustomerViewModel.kt`)
- Se implementó la sección de advertencia de **Zona de Peligro** en `SettingsScreen`.
- Fila interactiva **"Eliminar Cuenta de Socio"** en tono de advertencia (`ErrorRed`).
- Diálogo de confirmación modal (`AlertDialog`) que advierte claramente:
  - Cancelación definitiva e inmediata de saldo de puntos acumulados.
  - Revocación permanente del código QR de socio.
  - Bloqueo irreversible del correo electrónico para impedir nuevos registros en el club de lealtad.
- Conexión a `CustomerViewModel.deleteAccount()`: invoca la RPC segura `delete_customer_account()`, limpia sesión y almacén cifrado local sólo tras la confirmación remota, y regresa al usuario al estado invitado con notificación de estado.

### ANTIGRAVITY-16: Verificación, Pruebas y Despliegue Remoto
- **Despliegue Supabase:** Se aplicó la migración `20261023030503_customer_account_deletion_privacy.sql` con `supabase db push --linked --yes`.
- **Sincronización:** Se ejecutó `supabase migration list --linked`, confirmando las 33 migraciones idénticas entre local y remoto.
- **Compilación y Pruebas Android:**
  - `./gradlew :app:testDebugUnitTest`: **BUILD SUCCESSFUL**.
  - `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**.
- **Prueba en Emulador Android (`emulator-5554`):**
  - APK desplegado y ejecutado en vivo.
  - Se validó visualmente el banner de WhatsApp, el modal de términos legales y el diálogo de advertencia de eliminación de cuenta.

---

## 3. Arquitectura de Notificaciones en Tiempo Real (Análisis Técnico)

Ante la consulta sobre el uso de Supabase Realtime vs. Firebase Cloud Messaging (FCM):

1. **Supabase Realtime (WebSockets):**
   - Excelente para reactividad **in-app** (cuando el socio tiene la app abierta y en primer plano).
   - En Android, cuando la app se minimiza o se bloquea la pantalla, el sistema operativo (Doze mode) suspende los sockets y conexiones de red en segundo plano para ahorrar batería.
2. **Firebase Cloud Messaging (FCM):**
   - **Indispensable** para alertas del sistema, notificaciones push cuando la app está cerrada, recordatorios de vencimiento de periodo semestral o acreditación de puntos en caja.
3. **Recomendación de Arquitectura:**
   - Mantener Supabase como motor transaccional y disparar un Webhook de base de datos (`Database Webhook`) o Edge Function que consuma la API HTTP v1 de FCM para despachar las notificaciones a los dispositivos móviles.

---

## 4. Estado de Archivos y Artefactos

| Archivo / Componente | Estado |
|---|---|
| `HomeScreen.kt` | Modificado (WhatsApp banner agregado, carrusel de promociones removido) |
| `SettingsScreen.kt` | Modificado (Términos legales enlazados a diálogo, baja de cuenta con diálogo de confirmación) |
| `CustomerViewModel.kt` | Modificado (Integración de `deleteAccount()` con feedback UI) |
| `MainScreen.kt` | Modificado (Propagación de estado y eventos a `SettingsScreen`) |
| `20261023030503_customer_account_deletion_privacy.sql` | **Desplegado y sincronizado en Supabase remoto** (33/33) |
