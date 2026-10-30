# Entrega Antigravity — Sprint 04: Experiencia UI de Recompensas Semestrales, Historial y Novedades

> **Fecha:** 22 de octubre de 2026  
> **Tareas completadas:** [ANTIGRAVITY-10], [ANTIGRAVITY-11], [ANTIGRAVITY-12]  
> **Estado:** Compilación y pruebas unitarias exitosas (`BUILD SUCCESSFUL`). Probado en vivo en emulador Android (`emulator-5554`). Sincronización completa con la capa de datos de Codex (CODEX-13 a CODEX-17).

---

## 1. Resumen de Implementación

En este sprint se implementaron y verificaron las mejoras de interfaz y experiencia de usuario (UI/UX) para acompañar la lógica semestral estilo Cinépolis (Ene-Jun / Jul-Dic), el historial de visitas/puntos y el ordenamiento del Home screen sin duplicidad.

### Componentes y Vistas Desarrolladas

1. **Rediseño de [`HomeScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/home/ui/HomeScreen.kt) [ANTIGRAVITY-10]:**
   - **Eliminación de duplicidad:** Se removió la lista vertical duplicada de promociones que saturaba la pantalla.
   - **Carrusel Horizontal con Botón "Ver todas $\rightarrow$":** Se añadió un encabezado con botón de acceso rápido para explorar el catálogo completo en un modal sheet.
   - **Hoja Modal [`AllPromotionsSheet.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/home/ui/AllPromotionsSheet.kt):** Modal bottom sheet con scroll que lista todas las promociones vigentes con sus condiciones y fechas límite.
   - **Mini-sección "Nuestra Sucursal":** Tarjeta flat con dirección física (Matriz Uruapan), horarios de atención (Lunes a Domingo 9:00 - 18:00 hrs) y botón de marcación directa telefónica (`Intent.ACTION_DIAL`).
   - **Mini-sección "Sitio Web Oficial":** Banner corporativo con acceso directo en navegador al dominio del cliente (`https://laexquissita.com`).

2. **Catálogo de Recompensas Semestral en [`LoyaltyScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/loyalty/ui/LoyaltyScreen.kt) [ANTIGRAVITY-11]:**
   - **Insignia del Periodo:** Muestra el semestre activo (ej. `Periodo Jul - Dic 2026`) obtenido dinámicamente desde el ViewModel/Codex.
   - **Badges de Estado de Recompensa:**
     - *Disponible:* Puntos suficientes y no canjeada en el semestre.
     - *Canjeada en este periodo:* Ya utilizada durante el semestre activo (regla Cinépolis de 1 por semestre).
     - *Faltan X pts:* Si los puntos del cliente no alcanzan el costo.
   - **Diálogo Informativo de Canje:** Diálogo modal que se abre al tocar una recompensa disponible o bloqueada, explicando claramente que los canjes se solicitan directamente en caja al pagar mostrando el QR permanente.
   - **Estados Reactivos:** Indicador de carga (`loadingRewards`), estado vacío estilizado y manejo de errores con botón de reintento.

3. **Historial de Movimientos en [`ProfileScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/profile/ui/ProfileScreen.kt) [ANTIGRAVITY-12]:**
   - **Feed Cronológico:** Despliegue de los últimos movimientos del cliente (visitas, canjes, bienvenida y ajustes).
   - **Tarjeta de Movimiento ([`PointMovementCard`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/features/profile/ui/ProfileScreen.kt)):**
     - Iconografía por tipo de movimiento (`QrCodeScanner` para visitas, `CardGiftcard` para canjes, `Stars` para bienvenida, `History` para ajustes).
     - Píldora con delta de puntos (`+X pts` o `-X pts`).
     - Formato de fecha legible en español con hora (`d MMM yyyy, HH:mm`).
   - **Estados de Interfaz:** Spinner de carga, mensaje amigable ante historial vacío y opción de reintento ante errores de conexión.

4. **Orquestación en [`MainScreen.kt`](file:///C:/Users/MACHENIKE/AndroidStudioProjects/LaExquisssita/app/src/main/java/com/mirandadevsource/laexquisssita/ui/MainScreen.kt):**
   - Vinculación fluida entre `CustomerUiState` y las pantallas activas, garantizando que el refresco automático y manual actualice promociones, recompensas e historial en paralelo.

---

## 2. Verificación y Calidad

- **Reglas de Diseño:** 
  - Paleta Pantone oficial (`VanillaCustard`, `BrandyBrown`, `ChocolateFondant`, `ApricotIllusion`, `OfflineAmber`, `ErrorRed`, `WarmWhite`).
  - Cero emojis en toda la aplicación.
  - Diseño Flat consistente (`0.dp` elevation).
- **Pruebas de Compilación:**
  - `gradlew.bat :app:assembleDebug`: **BUILD SUCCESSFUL**.
  - `gradlew.bat :app:testDebugUnitTest`: **BUILD SUCCESSFUL** (13 pruebas unitarias pasando).
- **Base de Datos y Migraciones:**
  - Migración `20261022190359_enforce_semester_reward_redemption.sql` desplegada y sincronizada contra el proyecto remoto de Supabase mediante Supabase CLI.
- **Validación Visual en Emulador:**
  - `screen_home_full.png`: Verificada la distribución limpia de carrusel, sucursal y sitio web.
  - `screen_all_promos.png`: Verificada la apertura y navegación del modal sheet de promociones.
  - `screen_loyalty_sprint04.png`: Verificada la tarjeta permanente, badge de periodo semestral y catálogo.
  - `screen_profile_sprint04.png`: Verificada la información de perfil, configuración y sección de historial reciente.
