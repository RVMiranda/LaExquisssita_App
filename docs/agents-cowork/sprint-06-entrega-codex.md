# Entrega Codex — Sprint 06: notificaciones push FCM

> 24 de octubre de 2026. CODEX-22 a CODEX-24 implementados en la cadena local de `ExquisssitaAppManager`. Aún no se ha desplegado la migración ni la Edge Function.

## Implementación

- **CODEX-22:** `20261024201145_customer_push_notifications.sql` crea `public.dispositivos_fcm`, RLS por `clientes.user_id = auth.uid()`, `registrar_dispositivo_fcm`, `eliminar_dispositivo_fcm` y `obtener_tokens_fcm` restringida a `service_role`. La baja de cuenta purga los tokens mediante trigger.
- **CODEX-23:** `supabase/functions/send-push-notification/index.ts` verifica un secreto de webhook, consulta solo tokens activos, obtiene un token OAuth2 de la cuenta de servicio y manda FCM HTTP v1 al proyecto `laexquisssita-app`. El token OAuth se cachea por isolate. El error FCM `UNREGISTERED` elimina el token obsoleto.
- **CODEX-24:** `puntos_movimientos` notifica por `visita` y ajuste positivo. `promociones` notifica al insertarse activa o cambiar de inactiva a activa. Los mensajes predeterminados no usan emojis. `pg_net` entrega los webhooks fuera de la transacción de caja.

## Contrato para Antigravity

- El canal Android esperado por el payload se llama `canal_la_exquisssita`.
- Tras autenticar y obtener permiso de notificaciones, registrar el token con `registrar_dispositivo_fcm(p_token, p_modelo)`. Al cerrar sesión, llamar `eliminar_dispositivo_fcm(p_token)` antes de perder el JWT. La baja borra todos los dispositivos en backend.
- No insertar, modificar ni borrar filas directamente. La RPC de enumeración de destinatarios está reservada para el backend.
- El FCM `notification` contiene título y cuerpo; `data` contiene `type`, identificador de movimiento/promoción y valores de puntos como cadenas. Mantener títulos/cuerpos de cero emojis y dirigir el `PendingIntent` a la pantalla pertinente.
- `app/google-services.json` identifica el cliente Android. El envío requiere la clave privada JSON de una cuenta de servicio Firebase, guardada solo en secretos de Edge Function.

## Validación y despliegue pendiente

- `supabase migration list --linked`: 33/33 migraciones existentes coinciden; Sprint 06 aparece solo local (34 local / 33 remoto).
- `release06_push_notifications.test.sql` contiene 9 aserciones de RLS, registro idempotente, desvinculación y purga al borrar la cuenta; pgTAP no se ha ejecutado porque el motor Docker local no está disponible.
- Antes de desplegar, completar `supabase/functions/.env` desde `.env.example`, ejecutar `supabase secrets set --env-file supabase/functions/.env`, guardar en Vault `fcm_push_function_url` y el mismo `PUSH_WEBHOOK_SECRET` bajo `fcm_push_webhook_secret`, desplegar primero la Edge Function y luego aplicar la migración. Ejecutar pgTAP para Sprint 05/06 y `supabase db advisors` en una base local limpia.
- No se ejecutó `db reset`, `db push` ni `functions deploy`; no se modificó Supabase remoto.
