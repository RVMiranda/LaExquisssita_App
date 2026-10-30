# Sprint 05 — Cargas de Trabajo: Eliminación de Cuenta, Marco Legal y Contacto Directo

> **Fecha:** 22 de octubre de 2026  
> **Alcance:** Implementación de baja/eliminación de cuenta con cumplimiento legal y prevención de re-registro (Soft Delete + Anonimización), banner de WhatsApp en Inicio, visualización interactiva de términos legales en Configuración y arquitectura de notificaciones en tiempo real.

---

## 1. Análisis Técnico y Arquitectura de Solución

### 1.1. Análisis: Hard Delete vs. Soft Delete y Prevención de Re-registro de Correo
- **Problema del Hard Delete (`DELETE FROM auth.users` y tablas en cascada):**
  - Rompe la integridad referencial y financiera en el sistema de caja (`puntos_movimientos`, `visitas_qr`, `canjes_recompensas` y órdenes asociadas). Si se eliminan físicamente los registros, los cortes de caja, auditorías y contabilidad no cuadran.
  - Además, si se borra el registro de `auth.users`, el correo queda libre en Supabase Auth, lo que permitiría a usuarios abusar de promociones de bienvenida o beneficios registrándose, canjeando, borrando su cuenta y volviéndose a registrar con el mismo correo.
- **Solución Recomendada (Soft Delete con Anonimización + Trigger de Exclusión):**
  - En el esquema canónico actual, el perfil está en `public.clientes` (no existe `clientes_perfiles`). Los datos visibles se limpian, se revoca `qr_token_hash` y se agrega `deleted_at`.
  - **Integridad del Ledger:** El historial de puntos y visitas se preserva intacto para efectos contables, pero su saldo disponible se cancela mediante un movimiento de ajuste.
  - **Bloqueo de Re-registro en Supabase:** Se crea `public.correos_bloqueados_registro`, guardando un HMAC del correo normalizado con una clave privada en Vault, y un trigger `BEFORE INSERT ON auth.users`. Un registro nuevo con ese correo es rechazado por el trigger.
  - Auth conserva un tombstone `deleted_at` para no romper las referencias históricas con `ON DELETE RESTRICT`; se limpian identidad, sesiones y credenciales. Un JWT previamente emitido puede seguir siendo válido hasta expirar, así que RLS y los RPC de perfil/QR también deben rechazar la cuenta dada de baja.

### 1.2. Análisis: Notificaciones en Tiempo Real (Supabase Realtime vs. Firebase FCM)
- **Supabase Realtime:**
  - Opera mediante WebSockets y funciona en tiempo real **únicamente mientras la app está abierta en primer plano (*foreground*)**.
  - En cuanto la app se cierra o pasa a segundo plano, el sistema Android suspende los sockets para ahorro de batería (*Doze mode*). Por tanto, **Supabase por sí solo NO puede generar notificaciones en la barra de estado de Android con la app cerrada**.
- **Firebase Cloud Messaging (FCM):**
  - Es el estándar nativo de Android. Google Play Services mantiene la conexión del sistema para despertar al dispositivo y mostrar notificaciones visuales en la cortina superior.
- **Arquitectura Recomendada:**
  - *Foreground (Dentro de la app):* Supabase Realtime para refresco reactivo instantáneo del saldo y tarjeta.
  - *Background (Fuera de la app / Sistema):* Supabase Database Webhook en `puntos_movimientos` / `promociones` $\rightarrow$ Supabase Edge Function $\rightarrow$ Firebase FCM v1 API $\rightarrow$ Notificación en barra de Android.

---

## 2. Asignación de Tareas por Agente

### Responsabilidad de Codex (Capa de Datos y Backend)
- [x] **[CODEX-18]** Migración en `ExquisssitaAppManager` para tabla `public.correos_bloqueados_registro` y trigger `BEFORE INSERT ON auth.users` que impide registrar correos dados de baja.
- [x] **[CODEX-19]** RPC `delete_customer_account()` con seguridad `SECURITY DEFINER`:
  - Determina al cliente desde `auth.uid()`; no acepta IDs de cuenta suministrados por el cliente y rechaza cuentas de personal.
  - Registrar el correo en `correos_bloqueados_registro`.
  - Anonimiza `public.clientes`, revoca el QR permanente almacenado y oculta el historial al dueño dado de baja.
  - Registrar ajuste de saldo a cero en `puntos_movimientos`.
  - Marca `auth.users.deleted_at`, anonimiza Auth y elimina identidades, sesiones y factores de autenticación.
- [x] **[CODEX-20]** Contrato `AuthGateway` / `AuthRepository` para invocar `delete_customer_account` y ejecutar desconexión y limpieza local.
- [x] **[CODEX-21]** Pruebas unitarias, compilación y comparación de migraciones mediante Supabase CLI. La prueba pgTAP permanece pendiente porque Docker local no está disponible.

### Responsabilidad de Antigravity (Capa de Presentación y UX)
- [x] **[ANTIGRAVITY-13]** En `HomeScreen.kt`, remover el carrusel redundante de promociones (ahora exclusivo en pestaña *Promociones*) e integrar el `WhatsAppContactBanner` de atención directa y pedidos para llevar.
- [x] **[ANTIGRAVITY-14]** En `SettingsScreen.kt`, conectar las opciones "Aviso de Privacidad" y "Términos del Club de Lealtad" a [`LegalDialog.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/auth/ui/LegalDialog.kt) interactivo con scroll.
- [x] **[ANTIGRAVITY-15]** En `SettingsScreen.kt` y `CustomerViewModel`, implementar la sección "GESTIÓN DE CUENTA Y PRIVACIDAD" con botón de eliminación y `AlertDialog` de confirmación con advertencia de irreversibilidad.
- [x] **[ANTIGRAVITY-16]** Compilación y pruebas unitarias validadas (`BUILD SUCCESSFUL`). Verificación visual en vivo en emulador Android (`emulator-5554`).
