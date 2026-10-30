# Entrega Codex — Sprint 03: autenticación híbrida y tarjeta de lealtad

> Actualizado el 21 de octubre de 2026. CODEX-08 a CODEX-12 implementados. La UI se conserva para Antigravity.

## Integración para Antigravity

Usar la instancia Activity-scoped de `CustomerViewModel` ya compartida por `MainScreen`:

```kotlin
val customer: CustomerViewModel = viewModel(factory = app.container.customerViewModelFactory)
val auth by customer.authState.collectAsStateWithLifecycle()
val customerUi by customer.state.collectAsStateWithLifecycle()
```

- Login: `customer.signInWithEmail(email, password)`.
- Registro: `customer.signUpWithEmail(email, password, nombre, telefono)`; enviar teléfono vacío como `null`.
- Google continúa con `customer.signIn(activity)`.
- Deshabilitar los campos y botones mientras `customerUi.isAuthenticating`.
- Presentar `customerUi.authError` junto al formulario; al descartarlo/editar el formulario llamar `customer.clearAuthError()`.
- `customerUi.message` sigue disponible para mensajes transitorios del resto del módulo. Con confirmación de correo activada, el alta muestra ahí “Tu cuenta se creó. Confirma tu correo y después inicia sesión para activar tu tarjeta.”; la cuenta permanece invitada hasta verificar y entrar.
- Cerrar el sheet cuando `auth is AuthState.Authenticated`; no cerrarlo por éxito de la llamada Auth si el perfil sigue cargando.
- En `AuthState.ProfileError`, ofrecer `customer.refresh()` para reintentar `ensure_customer_profile`; la sesión puede ser válida aunque la RPC falle temporalmente.
- Después de `AuthState.Authenticated`, `CustomerViewModel` recupera el QR permanente por el flujo existente. La UI puede representar el estado intermedio con `customerUi.loadingCard`.

Los mensajes cubren correo inválido, contraseña menor a seis caracteres, credenciales incorrectas, correo ya registrado, contraseña rechazada por política, red y confirmación pendiente. No mostrar textos internos de Supabase.

## Contrato y seguridad

- `AuthGateway` conserva Google y añade email/password; `SupabaseAuthGateway` usa `signInWith(Email)` y `signUpWith(Email)` con metadatos `full_name` y teléfono opcional. El teléfono permite completar el perfil si el correo requirió confirmación antes del primer inicio de sesión.
- `AuthRepository.signUpWithEmail` valida entrada, serializa operaciones de autenticación y llama `CustomerRemote.ensureProfile(nombre, telefono)` solamente cuando Supabase ya tiene una sesión. El resultado exitoso actualiza el estado autenticado y la caché cifrada del perfil.
- Supabase requiere confirmar correo por defecto en proyectos hosted: si está activo, `signUpWith` no crea sesión automáticamente. El gateway lo distingue y no intenta llamar la RPC protegida. Tras confirmar y hacer login, el flujo normal de perfil continúa.
- La sesión sigue usando `SessionStore` cifrado existente. No se persiste la contraseña ni se amplían permisos de base de datos.
- `ensure_customer_profile` exige correo confirmado y solo crea/recupera el perfil de `auth.uid()`. Antigravity reporta que Release 03 ya se aplicó en remoto; este entorno no pudo contrastar el estado con Supabase CLI.

## Archivos y pruebas

- CODEX-08: `data/auth/AuthGateway.kt`, `data/auth/SupabaseAuthGateway.kt`.
- CODEX-09: `domain/repository/AuthRepository.kt`, `data/repository/AuthRepositoryImpl.kt`; reutiliza `CustomerRemote.ensureProfile`.
- CODEX-10: `features/auth/viewmodel/CustomerViewModel.kt`, agrega `isAuthenticating`, `authError`, métodos email y limpieza del error.
- CODEX-11: `app/src/test/java/.../AuthRepositoryTest.kt`; login email, perfil/teléfono, confirmación pendiente y perfil diferido con teléfono, credenciales y fallo de red del RPC.

Verificación repetida en esta continuación: `:app:testDebugUnitTest` y `:app:assembleDebug` finalizaron en **BUILD SUCCESSFUL**. La capa Kotlin ya contiene el mapeo de QR requerido.

## CODEX-12 — QR persistente

- `SupabaseCustomerRemote.recoverQr()` ya llama `get_or_create_customer_qr_token()`; `CustomerQr` decodifica `cliente_id` y `qr_token`. No hace falta alterar el mapeo Kotlin.
- La RPC está definida en `supabase/migrations/20261018210319_customer_permanent_qr_recovery.sql`: token aleatorio de 256 bits, secreto conservado en Vault, hash SHA-256 compatible con el escáner del Manager, bloqueo por fila para concurrencia, verificación del propietario/correo y rechazo seguro de tarjetas legacy sin secreto recuperable.
- Se corrigió un permiso requerido por la llamada anidada: el wrapper `SECURITY INVOKER` necesita `EXECUTE` del helper privado para `authenticated`. El helper no acepta IDs y valida `auth.uid()`; la tabla sigue sin SELECT y `private_customer` debe permanecer fuera de los esquemas expuestos por PostgREST.
- `customer_permanent_qr.test.sql` ahora tiene 15 aserciones, incluyendo el permiso del helper. No se pudo correr pgTAP en este turno porque falta el CLI de Supabase y la carpeta canónica del Manager está fuera de la raíz escribible.
- **Pendiente de entrega compartida:** integrar esta migración en `ExquisssitaAppManager/supabase/migrations`, correr reset limpio, pgTAP, concurrencia y advisors, luego verificar local/remoto y desplegar desde el proyecto Manager. No se ejecutó el push remoto.

Referencia del comportamiento de sesión y confirmación: [Supabase Kotlin — registro de usuario](https://supabase.com/docs/reference/kotlin/v2/auth-signup), [Supabase Auth — contraseñas y confirmación de correo](https://supabase.com/docs/guides/auth/passwords).
