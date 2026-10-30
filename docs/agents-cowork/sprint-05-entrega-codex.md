# Entrega Codex — Sprint 05: baja de cuenta

> 22 de octubre de 2026. CODEX-18 a CODEX-21 implementados. La migración aún requiere pgTAP local antes de desplegarse.

## Implementación

- CODEX-18: migración canónica del Manager `20261023030503_customer_account_deletion_privacy.sql`. Crea la tabla privada por RLS `public.correos_bloqueados_registro` con HMAC del correo normalizado y un pepper aleatorio en Vault; el trigger `BEFORE INSERT ON auth.users` bloquea re-registros.
- CODEX-19: `delete_customer_account()` determina al usuario solo con `auth.uid()`, rechaza cuentas admin/gerente y empleados activos, agrega al ledger un ajuste negativo por el saldo vigente, anonimiza `public.clientes`, revoca el digest y el secreto QR, elimina roles/sesiones/identidades/factores y marca `auth.users.deleted_at`. La fila Auth y el perfil tombstone se conservan por las referencias históricas `ON DELETE RESTRICT`; la RPC es segura para reintentos.
- CODEX-20: `AuthGateway` y `SupabaseAuthGateway` invocan la RPC; `AuthRepository.deleteAccount()` limpia sesión y caché local tras el éxito remoto. `CustomerViewModel.deleteAccount()` usa ese flujo y conserva la sesión si la RPC falla. El alta muestra un mensaje específico cuando Auth devuelve el marcador de correo bloqueado.
- CODEX-21: pruebas de repositorio para baja exitosa, desconexión y vaciado de caché, además de fallo remoto sin desconectar. La prueba SQL pgTAP tiene 13 aserciones para anonimización, ajuste de saldo, revocación del QR, limpieza de roles, bloqueo de movimientos posteriores a la baja, rechazo de JWT antiguo, repetición idempotente y bloqueo de correo.

## Verificación

- `:app:testDebugUnitTest` y `:app:assembleDebug`: **BUILD SUCCESSFUL**.
- `supabase migration list --linked`: las 31 migraciones desplegadas coinciden; `20261023030503` aparece solo en local y es el único pendiente.
- `supabase test db --local supabase/tests/release05_account_deletion.test.sql`: pendiente. El motor Docker local no está disponible; el comando no pudo conectar con `127.0.0.1:54322`.
- No se hizo `db reset`, `db push` ni ninguna escritura contra Supabase remoto. Ejecutar la cadena y pgTAP en una base local limpia, revisar `db advisors`, y después desplegar desde `ExquisssitaAppManager`.

## Contrato UI

La UI existente llama `CustomerViewModel.deleteAccount { ... }`. El callback se ejecuta solo después de que la RPC confirme y la sesión/cachés queden limpios; en caso de error se conserva la cuenta y `CustomerUiState.message` recibe el error. No se requiere cambio visual adicional para la tarea Codex.
