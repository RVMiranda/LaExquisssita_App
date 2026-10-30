# Plan de Implementación — La Exquisssita

> Actualizado el 22 de octubre de 2026.

## Base existente (compilación debug, pruebas de datos y lint validados)

- Dependencias declaradas de Compose, Supabase, Credential Manager, ZXing y Coil.
- Tema, navegación inferior y pantallas base de Novedades, Mi Tarjeta, Perfil y Configuración.
- Interfaces/modelos, repositorios iniciales, `AppContainer`, `SessionStore`, generador QR y utilidad de galería.
- Inyección manual con `AppContainer` y persistencia cifrada por cliente.

## Fase 1 — Autenticación Google y Tarjeta Permanente (Completada en UI/VM)

Asignación detallada: [Sprint 01](agents-cowork/sprint-01-cargas-de-trabajo.md).

- [x] Codex: contratos de estados/eventos, sesión reactiva y `CustomerViewModel`.
- [x] Codex: Web Client ID configurado en `local.properties` y Supabase.
- [x] Antigravity: UI de Mi Tarjeta conectada con renderizado dinámico de QR vía ZXing.
- [x] Antigravity: elevación de brillo automático en tarjeta y guardado en galería.

## Fase 2 — Promociones públicas y Novedades (Completada)

Asignación detallada: [Sprint 02](agents-cowork/sprint-02-cargas-de-trabajo.md).

- [x] Codex: repositorio de promociones vigentes de Supabase con respaldo local cifrado offline.
- [x] Codex: `HomeViewModel` con estados reactivos y pruebas unitarias.
- [x] Antigravity: carrusel y lista de promociones en `HomeScreen` con Coil y badges de vigencia.
- [x] Antigravity: `PromotionDetailSheet` modal con términos y redirección a tarjeta.

## Fase 3 — Autenticación Híbrida y Registro Manual (Completada)

Asignación detallada: [Sprint 03](agents-cowork/sprint-03-cargas-de-trabajo.md).

- [x] Codex: extender `AuthGateway` y `SupabaseAuthGateway` con `signUpWith(Email)` y `signInWith(Email)`.
- [x] Codex: implementar métodos en `AuthRepository` conectando `ensure_customer_profile` y emisión de QR (`CODEX-12`).
- [x] Codex: exponer acciones y estados de error/progreso en `CustomerViewModel`.
- [x] Antigravity: crear `AuthBottomSheet` con tabs Iniciar Sesión / Crear Cuenta, validaciones y Google Sign-In.
- [x] Antigravity: modal de Términos y Condiciones y Aviso de Privacidad (`LegalDialog.kt`) con checkbox obligatorio.
- [x] Antigravity: conectar nuevo modal de autenticación a los accesos en `LoyaltyScreen` y `ProfileScreen`.
- [x] Ambos: despliegue de migraciones en Supabase y verificación en vivo de login, confirmación por Brevo SMTP y recuperación de QR.

## Fase 4 — Recompensas Semestrales, Historial en Vivo y Rediseño de Inicio (Completada)

Asignación detallada: [Sprint 04](agents-cowork/sprint-04-cargas-de-trabajo.md). Entregas: [Codex](agents-cowork/sprint-04-entrega-codex.md), [Antigravity](agents-cowork/sprint-04-entrega-antigravity.md) y [Ajustes de Mejora UI](agents-cowork/entrega-ajustes-mejora-ui.md).

- [x] Codex: verificación CLI de migraciones y catálogo remoto (`[CODEX-13]`).
- [x] Codex: elegibilidad Ene-Jun / Jul-Dic por fecha de negocio CDMX, canje único consultado en cliente y rechazado en la RPC de caja del Manager (`[CODEX-14]`).
- [x] Codex: consulta real de canjes e historial ordenado de `puntos_movimientos` (`[CODEX-15]`).
- [x] Codex: exposición reactiva en `CustomerViewModel` de recompensas, historial, periodo y estados de carga/error (`[CODEX-16]`).
- [x] Codex: pruebas unitarias de periodos, canje previo, saldo faltante e historial ordenado (`[CODEX-17]`).
- [x] Antigravity: rediseño de `HomeScreen` con carrusel limpio, vista completa modal "Ver todas", sección "Nuestra Sucursal" y banner de "Sitio Web Oficial" (`[ANTIGRAVITY-10]`).
- [x] Antigravity: catálogo dinámico de recompensas semestrales en `LoyaltyScreen` con estados disponible/canjeada/faltante y modal explicativo de canje en caja (`[ANTIGRAVITY-11]`).
- [x] Antigravity: historial cronológico interactivo de puntos y visitas en `ProfileScreen` con badges de acumulación/canje (`[ANTIGRAVITY-12]`).
- [x] Despliegue de migración `20261022190359_enforce_semester_reward_redemption.sql` aplicado exitosamente al proyecto remoto de Supabase.

## Fase 5 — Eliminación de Cuenta, Marco Legal y Contacto Directo (Completada)

Asignación detallada: [Sprint 05](agents-cowork/sprint-05-cargas-de-trabajo.md). Entregas: [Codex](agents-cowork/sprint-05-entrega-codex.md) y [Antigravity](agents-cowork/sprint-05-entrega-antigravity.md).

- [x] Codex: migración para tabla `correos_bloqueados_registro` y trigger `BEFORE INSERT ON auth.users` (`[CODEX-18]`).
- [x] Codex: RPC `delete_customer_account` con soft delete, anonimización, revocación de QR y ajuste de balance a cero (`[CODEX-19]`).
- [x] Codex: integración en `AuthGateway`, `AuthRepository` y desconexión limpia (`[CODEX-20]`).
- [x] Codex: pruebas unitarias Kotlin y verificación CLI del historial de migraciones (`[CODEX-21]`).
- [x] Antigravity: banner de atención y pedidos por WhatsApp en `HomeScreen` (`[ANTIGRAVITY-13]`).
- [x] Antigravity: conexión de documentos legales interactivos en `SettingsScreen` (`[ANTIGRAVITY-14]`).
- [x] Antigravity: sección de gestión de cuenta y diálogo de confirmación de baja en `SettingsScreen` (`[ANTIGRAVITY-15]`).
- [x] Antigravity: verificación de compilación y pruebas unitarias (`[ANTIGRAVITY-16]`).
- [x] Despliegue de migración `20261023030503_customer_account_deletion_privacy.sql` aplicado exitosamente al proyecto remoto de Supabase (33/33 migraciones sincronizadas).

## Fase 6 — Notificaciones Push con Firebase (FCM v1) y Supabase (Completada al 100%)

Asignación detallada: [Sprint 06](agents-cowork/sprint-06-cargas-de-trabajo.md). Entregas: [Codex](agents-cowork/sprint-06-entrega-codex.md) y [Antigravity](agents-cowork/sprint-06-entrega-antigravity.md).

- [x] Codex: tabla `dispositivos_fcm`, RLS y RPCs de registro/desvinculación (`[CODEX-22]`).
- [x] Codex: Edge Function `send-push-notification` con autenticación OAuth2 Service Account para FCM v1 (`[CODEX-23]`).
- [x] Codex: disparadores y webhooks para notificación de acumulación de puntos y promociones (`[CODEX-24]`).
- [x] Antigravity: configuración de Gradle con plugin Google Services y Firebase Messaging (`[ANTIGRAVITY-17]`).
- [x] Antigravity: permisos Android 13+ y canal oficial de notificación (`[ANTIGRAVITY-18]`).
- [x] Antigravity: servicio `LaExquisssitaFirebaseMessagingService` con estilo de marca y cero emojis (`[ANTIGRAVITY-19]`).
- [x] Antigravity: vinculación del token FCM con el ciclo de vida de la sesión del socio (`[ANTIGRAVITY-20]`).
- [x] Despliegue de Edge Function `send-push-notification` y migración `20261024201145_customer_push_notifications.sql` sincronizados en producción (34/34 migraciones).

## Fase posterior — Google Wallet

- [ ] Diseñar e implementar pases con el mismo QR permanente.
- [ ] Validar configuración y emisión de pases antes de habilitar la acción.

Actualizar puntos, visitas o perfil nunca rota el QR. La reemisión por compromiso es excepcional y queda fuera del flujo ordinario del primer sprint.
