# Asignación de Cargas de Trabajo — Sprint 03: Autenticación Híbrida (Google + Correo) y Registro Manual

> **Proyecto:** La Exquisssita (App para Clientes)  
> **Ubicación:** `C:\Users\MACHENIKE\AndroidStudioProjects\LaExquisssita`  
> **Objetivo del Sprint:** Permitir a los clientes iniciar sesión o registrarse manualmente mediante formulario (Correo y Contraseña, Nombre y Teléfono opcional), manteniendo Google Sign-In como opción rápida. Al autenticarse por cualquier vía, se garantiza la vinculación con `ensure_customer_profile` y la generación/recuperación del QR permanente.

---

## 1. Arquitectura y Flujo de Autenticación

```
┌────────────────────────────────────────────────────────────────────────┐
│                   ANTIGRAVITY (UI & Experiencia)                       │
│  - AuthBottomSheet (ModalBottomSheet con tabs Login / Registro)        │
│  - Validación visual de formulario (email, password, nombre, teléfono) │
│  - Botón secundario "Continuar con Google" (One-Tap / Credential Mgr)   │
│  - Actualización de prompts en LoyaltyScreen y ProfileScreen           │
│  - Estados de progreso, validación y feedback de error sin emojis      │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Invoca métodos y consume UiState
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      CODEX (Data, Domain & VM)                         │
│  - AuthGateway con soporte para email/password (Supabase Auth)         │
│  - AuthRepository.signUpWithEmail y signInWithEmail                    │
│  - Invocación idempotente a ensure_customer_profile tras registro      │
│  - Métodos y estado reactivo en CustomerViewModel                      │
│  - Pruebas unitarias de flujo de autenticación manual                  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Tareas Asignadas a CODEX (Data, Backend & ViewModel)

### [CODEX-08] Soporte de Email/Password en AuthGateway y SupabaseAuthGateway
* **Archivos asignados:**
  * `data/auth/AuthGateway.kt`
  * `data/auth/SupabaseAuthGateway.kt`
* **Especificación:**
  * Agregar a `AuthGateway`:
    ```kotlin
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): EmailSignUpResult
    ```
  * En `SupabaseAuthGateway`:
    * Implementar `client.auth.signInWith(Email) { this.email = ...; this.password = ... }`.
    * Implementar `client.auth.signUpWith(Email) { this.email = ...; this.password = ...; data = buildJsonObject { put("full_name", nombre) } }`.
    * Mapeo de excepciones específicas de Supabase Auth a errores de negocio comprensibles (ej. usuario ya registrado, contraseña débil, credenciales erróneas).
    * El registro devuelve un resultado que distingue sesión inmediata de confirmación pendiente. Con confirmación pendiente no debe asumirse una sesión autenticada ni invocarse la RPC.

### [CODEX-09] Contratos e Implementación en AuthRepository
* **Archivos asignados:**
  * `domain/repository/AuthRepository.kt`
  * `data/repository/AuthRepositoryImpl.kt`
* **Especificación:**
  * Declarar en `AuthRepository`:
    ```kotlin
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?): Result<CustomerProfile>
    ```
  * En `AuthRepositoryImpl`:
    * Al registrarse con éxito, ejecutar inmediatamente la sincronización con `ensure_customer_profile(p_nombre = nombre, p_telefono = telefono)`.
    * Iniciar la carga o emisión del QR permanente igual que en el flujo de Google.
    * Guardar la sesión localmente y notificar `AuthState.Authenticated`.
    * Si el proyecto exige confirmar correo, mostrar el estado “revisa tu correo” y posponer la creación del perfil hasta que el usuario inicie sesión con correo confirmado. `ensure_customer_profile` valida `auth.users.email_confirmed_at`.

### [CODEX-10] Actualización de CustomerViewModel
* **Archivos asignados:**
  * `features/auth/viewmodel/CustomerViewModel.kt`
* **Especificación:**
  * Añadir funciones:
    ```kotlin
    fun signInWithEmail(email: String, password: String)
    fun signUpWithEmail(email: String, password: String, nombre: String, telefono: String?)
    fun clearAuthError()
    ```
  * Extender `CustomerUiState` o crear estado de soporte:
    * `authError: String? = null`
    * `isAuthenticating: Boolean = false`
  * Manejo seguro de concurrencia y cancelación de jobs previos.

### [CODEX-11] Pruebas Unitarias de Autenticación
* **Archivos asignados:** `test/java/.../AuthRepositoryTest.kt` (o equivalente)
* **Especificación:** Cobertura de casos: login exitoso con email, registro exitoso con creación de perfil, error por credenciales inválidas y error de red.

### [CODEX-12] RPC y Migración de Recuperación del QR Permanente
* **Archivos asignados:** `supabase/migrations/20261018210319_customer_permanent_qr_recovery.sql`
* **Especificación:**
  * Implementación de la RPC `get_or_create_customer_qr_token()`.
  * Generación y almacenamiento seguro en Supabase Vault (`private_customer.qr_secrets`).
  * Recuperación idempotente del mismo QR permanente entre sesiones, instalaciones y dispositivos.
  * Desplegado y verificado en la base de datos remota de Supabase.

---

## 3. Tareas Asignadas a ANTIGRAVITY (UI, Presentación & Componentes)

### [ANTIGRAVITY-08] Componente ModalBottomSheet de Autenticación
* **Archivos asignados:** `features/auth/ui/AuthBottomSheet.kt`
* **Especificación:**
  * `ModalBottomSheet` con diseño editorial Flat Premium (`VanillaCustard`, `BrandyBrown`, `ChocolateFondant`).
  * Selector / Tab superior: **Iniciar Sesión** | **Crear Cuenta**.
  * Formulario **Iniciar Sesión**:
    * Campo de Correo Electrónico (`OutlinedTextField`, validación de formato).
    * Campo de Contraseña (`OutlinedTextField`, visibilidad oculta/visible).
    * Botón primario: *"Iniciar Sesión"*.
  * Formulario **Crear Cuenta**:
    * Campo Nombre Completo.
    * Campo Correo Electrónico.
    * Campo Contraseña (mínimo 6 caracteres).
    * Campo Teléfono (opcional, teclado numérico de 10 dígitos).
    * Botón primario: *"Crear Mi Cuenta"*.
  * Divisor editorial con texto *"O continúa con"* y botón de **Google Sign-In**.
  * Alertas de validación y de error en `ErrorRed` nítidas y sin emojis.
  * Indicador de carga durante la autenticación deshabilitando entradas para evitar dobles envíos.

### [ANTIGRAVITY-09] Integración de Acceso en LoyaltyScreen y ProfileScreen
* **Archivos asignados:**
  * `features/loyalty/ui/LoyaltyScreen.kt`
  * `features/profile/ui/ProfileScreen.kt`
  * `ui/MainScreen.kt`
* **Especificación:**
  * Actualizar los banners y diálogos de invitado para ofrecer *"Iniciar Sesión o Registrarse"*.
  * Conectar el disparo de `AuthBottomSheet` cuando el usuario pulsa para unirse o iniciar sesión.
  * Al autenticarse, cerrar el sheet suavemente y mostrar de inmediato la Tarjeta Digital / Perfil del cliente.

---

## 4. Criterios de Aceptación Conjuntos

1. El usuario invitado puede elegir registrarse con correo/contraseña o continuar con Google.
2. Un registro manual exitoso crea de inmediato su perfil de cliente en Supabase y recupera/genera su tarjeta QR permanente.
3. El usuario puede cerrar sesión y volver a ingresar con su correo y contraseña o con Google recuperando exactamente el mismo QR.
4. Errores como contraseña corta, correo mal formado o credenciales incorrectas muestran mensajes claros y amigables.
5. Se mantiene estrictamente la estética Flat Premium, la paleta Pantone y la regla de **Cero Emojis**.

> **Dependencia de configuración/backend:** la creación inmediata de perfil y QR requiere que Supabase permita obtener sesión al registrarse (Confirm Email desactivado) y que las migraciones Release 03 con `ensure_customer_profile` y recuperación QR estén desplegadas. Si se mantiene Confirm Email, el flujo explícitamente espera confirmación e inicio de sesión antes de crear la tarjeta.
