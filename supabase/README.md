# Backend de clientes — integración con Manager

La cadena canónica de migraciones y funciones vive en `ExquisssitaAppManager/supabase`. Esta carpeta documenta el contrato del cliente; no es un proyecto Supabase independiente para `db push`.

Al 24 de octubre de 2026, `supabase migration list --linked` confirma 33 migraciones coincidentes entre local y remoto, incluyendo baja de cuenta Sprint 05. La migración de notificaciones `20261024201145_customer_push_notifications.sql` está solo en local (34 local / 33 remoto), pendiente de pgTAP y configuración de credenciales. No se desplegó al remoto.

La baja de cuenta conserva `public.clientes` y `auth.users` como tombstones por las referencias históricas. Anonimiza identidad y QR, cancela el saldo disponible con un movimiento auditable y bloquea el re-registro mediante HMAC del correo con pepper en Vault. RLS y los RPC de perfil, QR y lealtad rechazan sesiones antiguas.

## Notificaciones push

`dispositivos_fcm` guarda tokens Android y mantiene RLS por medio de `clientes.user_id = auth.uid()`; el UUID de `cliente_id` no es el UUID de Auth. Las escrituras se hacen con `registrar_dispositivo_fcm(p_token, p_modelo)` y `eliminar_dispositivo_fcm(p_token)`. La RPC `obtener_tokens_fcm(p_cliente_id)` solo es ejecutable por `service_role`. La baja de cuenta borra automáticamente todos los dispositivos.

Los movimientos `puntos_movimientos.tipo = 'visita'` y los ajustes positivos llaman `send-push-notification`; una promoción nueva ya activa o activada después también se difunde a todos los dispositivos con cuenta vigente. El flujo asíncrono usa `pg_net`, no bloquea la transacción de caja y crea mensajes sin emojis.

Antes de probar/desplegar, configurar `FCM_SERVICE_ACCOUNT`, `FCM_PROJECT_ID=laexquisssita-app` y `PUSH_WEBHOOK_SECRET` como secretos de la Edge Function. En Vault guardar `fcm_push_function_url` con el URL `https://<project-ref>.supabase.co/functions/v1/send-push-notification` y `fcm_push_webhook_secret` con el mismo secreto de webhook. El archivo `google-services.json` de Android solo identifica la app; no sustituye la clave privada de cuenta de servicio. No poner credenciales en Git ni en el cliente Android.

## Verificación

- `supabase migration list --linked`: 33 migraciones desplegadas coincidentes; Sprint 06 solo local.
- `supabase/tests/release06_push_notifications.test.sql`: 9 aserciones pgTAP para upsert idempotente, visibilidad/aislamiento, desvinculación y purga de dispositivos al eliminar cuenta. Pendiente de ejecutar en Docker local.
- No se ejecutó `db reset`, `db push` ni `functions deploy`. Tras iniciar Docker, aplicar en local limpio, correr pruebas Sprint 05 y 06, `supabase db advisors`, desplegar función y configurar secretos antes de `db push`.

Consulta [Sprint 06: entrega Codex](../docs/agents-cowork/sprint-06-entrega-codex.md), [especificación](../docs/agents-cowork/sprint-06-cargas-de-trabajo.md) y [contrato del backend](../docs/BACKEND-CONTRACT-CLIENT.md).
