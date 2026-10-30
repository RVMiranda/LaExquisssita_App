# Entrega Antigravity — Sprint 03: Autenticación Híbrida, Registro Manual y Cumplimiento Legal

> **Fecha:** 21 de octubre de 2026  
> **Tareas completadas:** [ANTIGRAVITY-08] y [ANTIGRAVITY-09] (Ampliadas con feedback de UX y marco legal)  
> **Estado:** Compilación y pruebas unitarias exitosas (`BUILD SUCCESSFUL`). Integrado con la capa de datos de Codex (CODEX-08 a CODEX-11).

---

## 1. Resumen de Implementación

Se completó y refinó la experiencia de inicio de sesión y registro de clientes con un flujo intuitivo de cero fricción, incorporando términos y condiciones legales y protección de privacidad conforme a los requerimientos del negocio.

### Novedades y Componentes Implementados

1. **Componente [`LegalDialog.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/auth/ui/LegalDialog.kt):**
   - Modal interactivo con scroll interno para lectura de documentos legales en tipografía artesanal.
   - Soporta dos documentos esenciales del negocio:
     - **Términos y Condiciones:** Objeto del Club La Exquisssita, carácter personal e intransferible del código QR, mecánica de acumulación de visitas y puntos, vigencia de promociones y política de uso.
     - **Aviso de Privacidad:** Identidad del responsable, datos recabados (nombre, correo, teléfono opcional e historial), fines exclusivos (lealtad y beneficios), garantía de no transferencia a terceros y derechos ARCO.

2. **Flujo de Registro con Redirección Suave en [`AuthBottomSheet.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/auth/ui/AuthBottomSheet.kt):**
   - **Pre-llenado de Correo:** Al registrarse, el correo electrónico ingresado se transfiere automáticamente a la pestaña de login.
   - **Vista de Notificación de Confirmación (`EmailConfirmationNoticeView`):**
     - Icono destacado de correo verificado (`MarkEmailRead`).
     - Resalta el correo electrónico exacto al que se envió el enlace.
     - **Contador regresivo visible (10 a 0 segundos):** Muestra al usuario *"Redirigiendo a Iniciar Sesión en X segundos..."*.
     - Botón de acción directa *"Ir a Iniciar Sesión Ahora"* por si el usuario prefiere avanzar de inmediato sin esperar el contador.
     - Al vencer el contador o pulsar el botón, transiciona suavemente a la pestaña **Iniciar Sesión** con el correo ya escrito.

3. **Consentimiento Legal y Checkbox Obligatorio:**
   - En el formulario de registro: Checkbox con texto enriquecido interactivo *"He leído y acepto los Términos y Condiciones y el Aviso de Privacidad."*
   - Validación antes del envío: Si el usuario no marca la casilla, se despliega una alerta amigable en `ErrorRed`.
   - En el acceso de Google: Leyenda legal en el pie del modal con enlaces clickeables a ambos documentos.

4. **Integración en Shell y Pantallas:**
   - [`MainScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/ui/MainScreen.kt): Orquestación reactiva del estado `isAuthSheetOpen`, auto-cierre al autenticarse o ante error de perfil con notificación Snackbar.
   - [`LoyaltyScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/loyalty/ui/LoyaltyScreen.kt) y [`ProfileScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/profile/ui/ProfileScreen.kt): Manejo robusto de `LoadingProfile` (sin fallos de casteo) y despliegue de `ProfileErrorView` con opción de reintento.

---

## 2. Despliegue de Migraciones en Supabase y Verificación de Flujo

1. **Migraciones Remotas Aplicadas Exitosamente:**
   - Se aplicaron las migraciones pendientes del catálogo y RPCs mediante Supabase CLI:
     - `20261017205614_release03_security_auth_loyalty_payroll_orders.sql`
     - `20261017205743_release03_payroll_and_order_price_guards.sql`
     - `20261017205843_release03_loyalty_ledger_customer_profiles_rewards.sql` (contiene `ensure_customer_profile` y `update_customer_profile`).
2. **Prueba en Vivo en Emulador (`emulator-5554`):**
   - Se validó el inicio de sesión con el usuario de prueba (`mirafaz24@outlook.com` / `Valentin Cruz`).
   - El perfil de cliente se generó y vinculó automáticamente en Supabase vía `ensure_customer_profile`.
   - Se visualizó correctamente la pantalla de perfil y la tarjeta de socio con 0 puntos y 0 visitas acumuladas.

---

## 3. Despliegue de CODEX-12 y Verificación Integral del QR Permanente

1. **Migración de CODEX-12 Desplegada a Supabase Remoto:**
   - Se integró `20261018210319_customer_permanent_qr_recovery.sql` en la cadena canónica de migraciones de `ExquisssitaAppManager`.
   - Se ejecutó el despliegue a la base de datos remota mediante `npx supabase db push`.
   - Quedaron activadas en Supabase la extensión `supabase_vault`, el esquema `private_customer`, la tabla `private_customer.qr_secrets` y la RPC segura `public.get_or_create_customer_qr_token()`.

2. **Prueba End-to-End en Emulador en Vivo (`emulator-5554`):**
   - Se pulsó el botón "Reintentar" en la pestaña "Mi Tarjeta".
   - La RPC devolvió con éxito el token permanente del usuario autenticado.
   - **Renderizado Visual:** El código QR se generó nítidamente con la paleta de marca (`ChocolateFondant` y fondo `VanillaCustard`).
   - **Acción "Guardar en Galería":** Se probó el guardado y se confirmó la creación de la imagen PNG en `/sdcard/Pictures/LaExquisssita/LaExquisssita_QR_*.png`.
   - **Sprint 03 queda 100% completado y verificado de punta a punta.**
