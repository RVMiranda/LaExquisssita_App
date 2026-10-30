# Contrato de Backend para la App de Clientes

> Actualizado el 24 de octubre de 2026. Backend compartido con ExquisssitaAppManager.
> Codex verificó que las 33 migraciones hasta Sprint 05 coinciden entre el Manager local y remoto. La migración Sprint 06 (`20261024201145`) está solo en local y aún no se ha desplegado.

## 1. Identidad y perfil

Google Sign-In está habilitado en Supabase según el propietario. Falta configurar/verificar el flujo Android con el Web Client ID correspondiente. Solo se utiliza una clave pública en la app.

### `ensure_customer_profile`

Crea o recupera idempotentemente el perfil de `auth.uid()` con correo confirmado. Parámetros: `p_nombre` obligatorio y `p_telefono` opcional. No vincular cuentas por teléfono.

Respuesta prevista según las migraciones locales de Manager:

```json
{"id":"uuid-del-cliente","nombre":"Juan Pérez","telefono":"5512345678","puntos_lealtad":0,"visitas_totales":0}
```

### `update_customer_profile`

Recibe `p_nombre` y `p_telefono` opcional; devuelve el perfil con los campos anteriores. Solo modifica nombre y teléfono, nunca puntos, visitas, identidad o QR. La captura de teléfono se puede omitir.

## 2. QR permanente recuperable — RPC implementada y verificada en remoto

### Requisito confirmado

Cada cliente conserva el mismo QR al cerrar sesión, reinstalar y entrar desde otro teléfono. Las imágenes descargadas y los futuros pases Wallet deben seguir representando esa tarjeta. Actualizar perfil, puntos o visitas no cambia el QR.

El QR contiene un token opaco, sin datos personales. Mostrarlo offline identifica la tarjeta; la validación/acreditación de visitas y canjes continúa a cargo del backend y Manager.

### Limitación del contrato local actual

La migración local de Manager define `rotate_customer_qr_token()` sin parámetros. Genera 256 bits aleatorios (64 caracteres hexadecimales), entrega el token una sola vez, guarda SHA-256 y revoca el anterior al emitir otro. Respuesta:

```json
{"cliente_id":"uuid-del-cliente","qr_token":"token-opaco-de-64-caracteres-hexadecimales"}
```

El hash por sí solo no permite recuperar el token original. Por tanto, esta RPC NO cumple la recuperación entre instalaciones y NO debe llamarse automáticamente cuando falte la caché.

### Contrato implementado

- `get_or_create_customer_qr_token()` sin parámetros, respuesta `{cliente_id, qr_token}`. Token cifrado en Vault y referencia en `private_customer.qr_secrets`; hash SHA-256 compatible con Manager.
- Devuelve el mismo token al mismo cliente, incluso ante solicitudes simultáneas desde dos teléfonos.
- Obtiene la identidad de la sesión; un cliente no puede solicitar la tarjeta de otro.
- La implementación privilegiada se mantiene en esquema privado y el wrapper público es SECURITY INVOKER. Recupera únicamente la tarjeta de auth.uid() verificado. La fila del cliente se bloquea durante emisión/recuperación. Un hash legacy sin secreto recuperable produce error, no una reemisión silenciosa.
- La reemisión excepcional por compromiso queda separada de la recuperación normal; no habrá botón de refresco que invalide el QR. Ese flujo excepcional queda fuera del primer sprint.
- El SQL de referencia está en `supabase/migrations/20261018210319_customer_permanent_qr_recovery.sql`; la migración también está en la cadena canónica del Manager. El CLI confirmó que esa versión está aplicada en remoto y el catálogo confirmó la RPC ejecutable por `authenticated`. El escáner del Manager conserva compatibilidad por el hash SHA-256.

### Caché y uso offline

La caché local se aísla por identidad y permite mostrar/descargar el QR tras la recuperación inicial. Cerrar sesión limpia los datos privados locales sin revocar la tarjeta remota; volver a entrar recupera el mismo código. Reinstalar o iniciar en un teléfono nuevo requiere conexión inicial. Reconectar actualiza puntos y visitas por separado.

## 3. Lecturas privadas

- Perfil: solo el del cliente autenticado.
- `recompensas`: recompensas activas; campos previstos `id`, `titulo`, `descripcion`, `costo_puntos`, `activo`.
- `puntos_movimientos`: solo movimientos del propio cliente; campos previstos `id`, `puntos_delta`, `tipo`, `motivo`, `created_at`.
- Visitas: restringir mediante la relación con el perfil del usuario. Validar nombres de columnas contra las migraciones reales; no asumir `usuario_id` en `visitas_clientes`.
- Ningún cliente puede modificar directamente saldos, visitas ni información del personal.

Verificación CLI de catálogo y permisos del 22 de octubre de 2026: RPC `ensure_customer_profile` y `get_or_create_customer_qr_token` presentes y ejecutables por `authenticated`; tablas de lealtad con RLS habilitado y políticas para el cliente autenticado. La app solo requiere SELECT en `recompensas`, `canjes_recompensas` y `puntos_movimientos`; los permisos de tabla pueden ser de columna y no de tabla completa.

## 3.1 Recompensas semestrales y canje

- Periodos de negocio en `America/Mexico_City`: del 1 de enero al 1 de julio y del 1 de julio al 1 de enero siguiente. Las consultas usan inicio inclusivo y siguiente inicio exclusivo.
- La app lee `canjes_recompensas.recompensa_id` y `created_at`; RLS restringe esas filas al cliente autenticado. Un registro en el semestre activo marca la recompensa como canjeada; si no existe, el estado depende de `puntos_lealtad` frente a `costo_puntos`.
- El cliente no canjea desde la app. La caja llama `redeem_loyalty_reward`, que rechaza otro canje de la misma recompensa para el mismo cliente en ese semestre y conserva reintentos idempotentes. La migración `20261022190359_enforce_semester_reward_redemption.sql` ya está aplicada al remoto.

## 4. Promociones para invitados

El visitante sin sesión puede leer promociones generales vigentes. Codex debe concretar columnas, vigencia y mecanismo de lectura pública de `promociones` (o una interfaz dedicada), con permisos limitados a información publicable. Ese permiso todavía no está verificado en remoto.

No se necesita exponer `productos` ni inventario. El menú no es requisito; si se añade, puede ser contenido estático confirmado por el negocio.

## 5. Verificación requerida

Antes de declarar la integración lista: desplegar y probar migraciones, acceso por identidad, recuperación del mismo QR tras reinstalación/segundo dispositivo, concurrencia, aislamiento entre cuentas, lectura en Manager y promociones sin sesión. Google Wallet queda para una fase posterior.

Implementación y validación SQL previas: [migración y resultados](../supabase/README.md). El 22 de octubre el CLI confirmó la RPC QR en remoto y el catálogo confirmó los permisos de lectura requeridos.


Para Sprint 05, las pruebas unitarias Android y compilación debug pasaron; su migración ya aparece aplicada en el remoto. Para Sprint 06, la migración está solo local; faltan pgTAP y Docker, despliegue/configuración de la Edge Function y secretos FCM/webhook. Ver [entrega Sprint 06](agents-cowork/sprint-06-entrega-codex.md).

## 6. Dispositivos y notificaciones push

- `public.dispositivos_fcm`: `id`, `cliente_id`, `fcm_token`, `plataforma`, `modelo_dispositivo`, `actualizado_el`, `creado_el`. RLS relaciona `cliente_id` con la fila de `clientes` cuyo `user_id = auth.uid()` y rechaza cuentas con `deleted_at`.
- `registrar_dispositivo_fcm(p_token text, p_modelo text default null)`: upsert idempotente del token para el socio autenticado. El token puede trasladarse a la cuenta actual cuando el mismo dispositivo inicia sesión con otra cuenta.
- `eliminar_dispositivo_fcm(p_token text)`: desvincula el token del socio autenticado; debe llamarse antes de cerrar sesión si la sesión sigue disponible. La baja de cuenta purga automáticamente todos sus tokens.
- `obtener_tokens_fcm(p_cliente_id uuid default null)`: función privada para consultas del backend; solo `service_role` puede ejecutarla. Sin cliente, produce destinatarios de promociones para todos los clientes activos.
- Eventos: movimiento `visita` o ajuste de puntos positivo, y promoción creada activa o cambio de inactiva a activa. Movimientos negativos y promociones inactivas/vencidas no notifican.
- La Edge Function recibe título, cuerpo y datos, obtiene credenciales OAuth2 de una cuenta de servicio Firebase y envía FCM HTTP v1 al proyecto `laexquisssita-app`. `UNREGISTERED` elimina el token obsoleto.
- Configurar los secretos `FCM_SERVICE_ACCOUNT`, `FCM_PROJECT_ID`, `PUSH_WEBHOOK_SECRET` y Vault (`fcm_push_function_url`, `fcm_push_webhook_secret`) antes de activar el despacho. La clave de servicio nunca se incorpora a Android.

Estado: 33 migraciones remotas/locales coincidentes; Sprint 06 local solamente. Sus 9 aserciones pgTAP están escritas pero pendientes por disponibilidad de Docker. Revisar [la entrega de Codex](agents-cowork/sprint-06-entrega-codex.md) y [la guía Supabase](../supabase/README.md).

