# Arquitectura y Diseño — La Exquisssita (App para Clientes)

> **Aplicación Móvil Nativa Android para Clientes**  
> **Ubicación:** `C:\Users\MACHENIKE\AndroidStudioProjects\LaExquisssita`  
> **Stack:** Android Nativo, Kotlin, Jetpack Compose, Material 3, Supabase-kt, Google Credential Manager  
> **Backend Remoto:** Supabase / PostgreSQL (Instancia compartida con ExquisssitaAppManager)  

---

## 1. Visión y Principios del Proyecto

La app **La Exquisssita** está concebida como el canal digital de fidelización y novedades de la marca para sus clientes finales. Su objetivo es brindar una experiencia fluida, rápida y con una estética **elegante, artesanal y premium**, eliminando la fricción de registros tradicionales mediante **Google Sign-In en 1 solo tap**.

### Principios Fundamentales:
1. **Modo Invitado de Entrada:** El cliente puede abrir la app y navegar libremente en **Novedades** (promociones generales) sin registrarse. Solo al pulsar **"Mi Tarjeta"** o **"Perfil"** se le presenta el acceso con Google 1-Tap.
2. **Zero-Friction Onboarding:** Registro e inicio de sesión instantáneo con Google Credential Manager. Al ingresar, se genera o enlaza automáticamente el perfil del cliente en Supabase.
3. **Cero Emojis & Estilo Flat Premium:** Todo elemento visual mantiene un tono elegante y profesional. Se prohíbe el uso de emojis en toda la interfaz. La señalización se realiza con iconos vectoriales outline (`Icons.Outlined.*`) y tipografía cuidada.
4. **Navegación Foco por Barra Inferior (Bottom Navigation Bar):** Al ser una app de cliente con alcance directo, se sustituye el menú lateral (hamburguesa) por un `NavigationBar` inferior accesible con el pulgar con 3 destinos principales.
5. **Tarjeta y Código QR Permanente (Offline-Ready):** El código QR del cliente es permanente e intransferible. Se emite una sola vez por cliente y debe recuperarse desde el backend con el mismo valor tras reinstalar o cambiar de teléfono; la persistencia local es una caché para uso offline. La app ofrece la opción de **guardarlo en la galería de fotos** o, en una fase posterior, agregarlo a **Google Wallet** para que el cliente siempre lo tenga disponible en caja, incluso sin internet ni datos móviles.
6. **Brillo Máximo Automático en Tarjeta (Configurable):** Al abrir la vista de Mi Tarjeta, la app eleva automáticamente el brillo de la pantalla para garantizar una lectura inmediata del escáner en caja. Esta función puede activarse o desactivarse desde la nueva vista de **Configuración**.
7. **Seguridad y Privacidad Estricta (RLS):** La app opera exclusivamente bajo el rol `authenticated` del cliente con la clave pública de Supabase (`anon_key`). No tiene acceso a tablas operativas (nómina, turnos, inventario confidencial, cortes de caja).

---

## 2. Estructura de Navegación (Bottom Navigation Bar)

La aplicación implementa una estructura de navegación con **Scaffold** central y `NavigationBar` de Material 3 con 3 pestañas principales:

```
┌─────────────────────────────────────────────────────────────┐
│                    TopBar (La Exquisssita)                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                                                             │
│                      CONTENIDO ACTIVO                       │
│                                                             │
│  [1. Novedades]          [2. Lealtad]         [3. Perfil]   │
│   • Promociones           • Tarjeta QR         • Datos      │
│   • Menú destacado        • Puntos / Nivel     • Teléfono   │
│   • Anuncios              • Recompensas        • Sesión     │
│                                                             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  [★ Novedades]          [◆ Mi Tarjeta]        [👤 Perfil]   │
│                     Bottom Navigation Bar                   │
└─────────────────────────────────────────────────────────────┘
```

### Rutas de la App:
* **`AuthRoute`:** Pantalla de Bienvenida y Login con Google One-Tap + diálogo/hoja para completar teléfono de contacto (si no está registrado).
* **`MainScaffoldRoute`:** Contenedor de las 3 secciones principales:
  1. **`NovedadesRoute` (Inicio):**
     * Carrusel de promociones y descuentos vigentes (`promociones_activas`).
     * Promociones generales accesibles sin sesión. Menú opcional con contenido estático validado por el negocio, sin consultar inventario.
  2. **`LealtadRoute` (Tarjeta & QR):**
     * Tarjeta digital premium con gradiente/textura sutil, nombre del cliente y código de socio.
     * Generador de código QR nativo (ZXing) estable, con ajuste de brillo automático opcional. Actualizar puntos/visitas no cambia el QR.
     * Contador de puntos acumulados (`puntos_lealtad`) y visitas totales (`visitas_totales`).
     * Catálogo de recompensas canjeables (`recompensas`) con indicador de progreso.
  3. **`PerfilRoute`:**
     * Información del socio (Nombre, Correo de Google, Teléfono de contacto).
     * Edición de teléfono y nombre con validación en tiempo real (`update_customer_profile`).
     * Historial de transacciones de puntos y visitas registradas (`puntos_movimientos`).
     * Acceso a `ConfiguracionRoute`.
     * Botón de cierre de sesión seguro.
  4. **`ConfiguracionRoute` (Sub-vista de Ajustes):**
     * Switch: Activar/desactivar brillo máximo automático en la tarjeta.
     * Acción: Guardar código QR en Galería de fotos (con verificación de permisos de almacenamiento).
     * Acción: Agregar a Google Wallet (preparación de integración de pases).
     * Información de versión y términos de privacidad.

---

## 3. Sistema de Diseño (Design Tokens & Theme)

Se hereda y preserva la paleta de color oficial de **ExquisssitaAppManager**, adaptando los contrastes para ofrecer una experiencia prémium a los comensales.

### 3.1 Paleta de Colores
* **Vanilla Custard (`#FAF3E0`):** Fondo principal cálido, superficies de tarjetas y fondos limpios.
* **Brandy Brown (`#7E392F`):** Color de marca primario, botones de acción destacados, bordes de enfoque y acentos de navegación activa.
* **Chocolate Fondant (`#3D2723`):** Tipografía primaria, títulos de sección, números de saldo de puntos y textos destacados.
* **Apricot Illusion (`#E5C2A4`):** Bordes secundarios, separadores, tarjetas de lealtad, campos de formulario no enfocados.
* **Offline Amber (`#A07324`):** Acentos dorados para insignias de nivel de lealtad, puntos y promociones.
* **Error Red (`#B3261E`):** Alertas y validaciones críticas.

### 3.2 Reglas Visuales
* **Sin Elevación Excesiva:** Usar elevación plana o sutil (`0.dp` a `2.dp`) con bordes nítidos de `1.dp` en `ApricotIllusion` o `BrandyBrown` para otorgar un aspecto editorial y artesanal.
* **Tipografía:** Uso jerárquico de títulos con peso `FontWeight.Bold` o `SemiBold`, etiquetas de soporte en `FontWeight.Medium` y números tabulares claros para puntos y precios.
* **Iconografía:** Únicamente `Icons.Outlined.*` de Material Icons. Prohibido el uso de emojis en cualquier capa de UI.

---

## 4. Arquitectura de Código (Clean Architecture + MVI/MVVM)

El proyecto se estructura en capas desacopladas dentro del paquete base `com.mirandadevsource.laexquisssita`:

```
com.mirandadevsource.laexquisssita
├── core
│   ├── config (SupabaseConfig, BuildConfig wrappers)
│   ├── designsystem (Componentes reutilizables, ExquisssitaButton, ExquisssitaCard)
│   └── util (QRGeneratorUtil, ResultState)
├── data
│   ├── auth (GoogleCredentialManager, SupabaseAuthRepository)
│   ├── loyalty (LoyaltyRepository, DTOs, recuperación de QR permanente)
│   ├── menu (MenuRepository, PromocionesDTO)
│   └── profile (ProfileRepository)
├── domain
│   ├── model (CustomerProfile, LoyaltyCard, Reward, Promotion)
│   └── repository (Contratos/Interfaces de repositorios)
├── features
│   ├── auth (AuthScreen, AuthViewModel, AuthState)
│   ├── home (HomeScreen, HomeViewModel)
│   ├── loyalty (LoyaltyScreen, LoyaltyViewModel)
│   └── profile (ProfileScreen, ProfileViewModel)
├── navigation
│   ├── AppNavHost.kt
│   └── Screen.kt (BottomBar Destinations)
└── ui
    └── theme (Color.kt, Theme.kt, Type.kt)
```

---

## 5. Librerías y Dependencias Principales

1. **Jetpack Compose + Material 3:** Interfaz moderna y reactiva.
2. **Navigation Compose:** Manejo del NavHost y selección de pestañas.
3. **Supabase Kotlin SDK (`3.8.0`):**
   * `auth-kt`: Manejo de sesión con Google ID Token y persistencia de JWT.
   * `postgrest-kt`: Ejecución de RPCs (`ensure_customer_profile`, `update_customer_profile` y recuperación idempotente del QR pendiente de diseño; no usar rotación para recuperar) y consultas RLS.
4. **Google Credential Manager (`androidx.credentials` + `googleid`):** Autenticación nativa 1-tap sin Chrome Custom Tabs.
5. **ZXing Core (`com.google.zxing:core:3.5.4`):** Generación vectorial/matricial instantánea del código QR del cliente sin dependencias pesadas.
6. **Coil Compose:** Carga asíncrona de imágenes de promociones y platillos.
7. **Inyección de dependencias:** La base usa `core/di/AppContainer`; Hilt/Dagger es una opción futura, no un requisito del primer sprint.

## 6. Alcance y estado confirmados — 18 de octubre de 2026

El primer sprint incluye acceso Google, teléfono opcional, tarjeta real permanente, lectura offline tras recuperación inicial y descarga en galería. Wallet queda para una fase posterior. El QR no cambia con puntos/visitas ni con nuevas instalaciones; los datos de saldo y visitas se actualizan al tener conexión. Mostrar la tarjeta offline no implica validación de caja offline.

Las migraciones de clientes todavía no están desplegadas. Google Sign-In está habilitado en Supabase según el propietario; falta configurar/verificar Android. Las migraciones previas de Manager solo guardan el hash; Codex preparó una migración adicional de recuperación cifrada con Vault, aún pendiente de prueba SQL y despliegue. Ver [contrato objetivo](BACKEND-CONTRACT-CLIENT.md).

Codex implementa ViewModels, estados, eventos, datos, contratos y dependencias; Antigravity implementa pantallas y su conexión a esos estados. Ver [asignación exclusiva del sprint](agents-cowork/sprint-01-cargas-de-trabajo.md).

La estructura de capas anterior es una guía: la base real usa `data/repository`, `core/di/AppContainer` y `core/security/SessionStore` con SharedPreferences. CustomerViewModel ya está implementado y documentado en la entrega Codex. Se mantiene inyección manual y SharedPreferences cifradas con Android Keystore; Hilt/DataStore no forman parte de esta implementación.

