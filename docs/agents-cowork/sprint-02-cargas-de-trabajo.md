# Asignación de Cargas de Trabajo — Sprint 02: Novedades, Promociones y Catálogo

> **Proyecto:** La Exquisssita (App para Clientes)  
> **Ubicación:** `C:\Users\MACHENIKE\AndroidStudioProjects\LaExquisssita`  
> **Objetivo del Sprint:** Conectar la pestaña **Novedades (Inicio)** a promociones vigentes públicas de Supabase, con respaldo local y estado reactivo. El menú dinámico queda fuera de esta fase; no hay un contrato acordado para que la app de clientes consulte productos y el menú puede ser información estática cuando el negocio entregue su contenido definitivo.

---

## 1. Arquitectura y Flujo del Módulo

```
┌────────────────────────────────────────────────────────────────┐
│               AGENTE ANTIGRAVITY (UI & Experiencia)            │
│  - Rediseño editorial de HomeScreen (Pantone Flat Premium)     │
│  - Carrusel horizontal de promociones con Coil y dots          │
│  - Estados visuales para promociones y error/offline           │
│  - PromotionDetailSheet (ModalBottomSheet con términos y QR)   │
│  - Componentes de soporte en core/designsystem/                │
└───────────────────────────────┬────────────────────────────────┘
                                │ Consume HomeUiState & Eventos
                                ▼
┌────────────────────────────────────────────────────────────────┐
│                  AGENTE CODEX (Data & Domain)                  │
│  - Modelo de dominio Promotion (CatalogItem queda reservado)   │
│  - Contrato e implementación de PromotionRepository            │
│  - Consulta RLS a public.promociones con caché offline local   │
│  - HomeViewModel con StateFlow (promotions, category, status)  │
│  - Pruebas unitarias de repositorio y ViewModel                │
└────────────────────────────────────────────────────────────────┘
```

---

## 2. Tareas Asignadas a CODEX (Data, Backend & ViewModel)

### [CODEX-04] Modelos de Dominio de Promociones y Catálogo
* **Archivos asignados:**
  * `domain/model/Promotion.kt`
  * `domain/model/CatalogItem.kt`
* **Especificación:**
  * `Promotion`: `id` (String), `titulo` (String), `descripcion` (String), `terminosCondiciones` (String?), `imagenUrl` (String?), `fechaPublicacion` (String), `fechaVencimiento` (String?), `activo` (Boolean). Con anotaciones `@Serializable` y `@SerialName`.
  * `CatalogItem`: tipo reservado para una fase futura. No consultar ni mostrar productos en este Sprint sin contrato de catálogo y contenido aprobado por el negocio.

### [CODEX-05] Repositorio de Promociones con Respaldo Offline
* **Archivos asignados:**
  * `domain/repository/PromotionRepository.kt`
  * `data/repository/PromotionRepositoryImpl.kt`
* **Especificación:**
  * Consultar `public.promociones` filtrando por `activo = true` y ordenando por `fecha_publicacion` descendente.
  * Almacenar en caché cifrada/local las últimas promociones obtenidas para que la pestaña Novedades funcione instantáneamente en modo offline.
  * Manejo de errores con `Result<PromotionFeed>`; `isOffline` identifica el respaldo local.
  * Filtrar `activo = true` en Supabase y excluir localmente fechas de vencimiento anteriores al día local actual. Una promoción que vence hoy sigue vigente todo el día.

### [CODEX-06] HomeViewModel y Estado Reactivo
* **Archivos asignados:**
  * `features/home/viewmodel/HomeViewModel.kt`
* **Especificación:**
  * Exponer `state: StateFlow<HomeUiState>` con:
  * `promotions: List<Promotion>` (solo promociones activas y vigentes)
    * `selectedCategory` queda reservado hasta que exista catálogo dinámico; el UI no debe presentar categorías de menú ficticias en esta fase.
    * `isLoading: Boolean`
    * `isOffline: Boolean`
    * `errorMessage: String?`
  * Métodos: `loadData()`, `refresh()`, `onCategorySelected(category: String)`, `clearError()`.
  * Integrar en `AppContainer` dentro del factory correspondiente.

### [CODEX-07] Pruebas Unitarias de Promociones
* **Archivos asignados:** `test/java/.../PromotionRepositoryTest.kt`
* **Especificación:** Cobertura de recuperación offline, serialización de DTOs y filtrado por vigencia.

---

## 3. Tareas Asignadas a ANTIGRAVITY (UI, Presentación & Componentes)

### [ANTIGRAVITY-04] Carrusel de Promociones con Coil
* **Archivos asignados:** `features/home/ui/HomeScreen.kt`
* **Especificación:**
  * `LazyRow` con `snap` o desplazamiento suave mostrando las promociones activas.
  * Carga asíncrona de imágenes mediante `AsyncImage` (Coil) con contenedor en color `CreamSurface` y placeholder sutil.
  * Badges elegantes en `BrandyBrown` y `ApricotIllusion` para la vigencia (ej. "Válido hasta el domingo").
  * Indicador de posición (dots interactivos de página).

### [ANTIGRAVITY-05] Menú estático opcional (fuera de alcance de integración)
* **Archivos asignados:** `features/home/ui/HomeScreen.kt`, `core/designsystem/CategoryChip.kt`
* **Especificación:**
  * No consumir `productos` ni mostrar categorías/productos de ejemplo como si fueran el menú real.
  * El negocio indicó que el menú permanece estable desde hace años y que no es requisito para invitados. Puede agregarse como contenido estático solo cuando el negocio entregue/apruebe esos datos; no bloquear el feed de promociones.

### [ANTIGRAVITY-06] Modal de Detalle de Promoción / Platillo
* **Archivos asignados:** `features/home/ui/PromotionDetailSheet.kt`
* **Especificación:**
  * `ModalBottomSheet` que se abre al tocar una tarjeta de promoción o producto.
  * Muestra imagen en tamaño grande, descripción extendida, vigencia y términos y condiciones legales en tipografía `TextMuted`.
  * Botón de acción principal: *"Ver Mi Tarjeta para Canjear"* (redirige a la pestaña de Lealtad).

### [ANTIGRAVITY-07] Estados de Carga, Vacío y Error
* **Archivos asignados:** `features/home/ui/HomeScreen.kt`
* **Especificación:**
  * Shimmer / placeholder cálido mientras `isLoading == true`.
  * Empty state artesanal sin emojis si no hay promociones activas.
  * Botón de *"Reintentar"* con icono `Icons.Outlined.Refresh` ante fallos de conexión.

---

## 4. Matriz de No-Colisión de Archivos (Zero-Overlap)

| Archivo | Agente Responsable | Tipo de Intervención |
| :--- | :--- | :--- |
| `domain/model/Promotion.kt` | **Codex** | Creación de entidad inmutable |
| `domain/model/CatalogItem.kt` | **Codex** | Tipo reservado, sin consulta ni integración en esta fase |
| `domain/repository/PromotionRepository.kt` | **Codex** | Contrato de datos |
| `data/repository/PromotionRepositoryImpl.kt` | **Codex** | Consulta Supabase y caché |
| `features/home/viewmodel/HomeViewModel.kt` | **Codex** | Lógica de estado y eventos |
| `core/designsystem/*` | **Antigravity** | Chips, Badges, Botones flat |
| `features/home/ui/HomeScreen.kt` | **Antigravity** | Maquetación y consumo de UI |
| `features/home/ui/PromotionDetailSheet.kt` | **Antigravity** | ModalBottomSheet de detalle |
| `ui/MainScreen.kt` | **Antigravity** | Conexión del HomeViewModel |

---

## 5. Contrato de integración Codex → Antigravity (Sprint 02)

La implementación de datos se entrega en `docs/agents-cowork/sprint-02-entrega-codex.md`. `AppContainer.homeViewModelFactory` crea el ViewModel; la UI debe compartir una instancia de Activity/shell y observar `state` con `collectAsStateWithLifecycle()`.

Mapeo de cada `Promotion` a `PromotionItemUi`: `id`, `titulo → title`, `descripcion → description`, `fechaVencimiento → validity` (formatear para usuario; si es null mostrar vigencia sin fecha límite), `terminosCondiciones → terms`, `imagenUrl → imageUrl`. La fecha de publicación no es una fecha de caducidad. El `HomeUiState` incluye `isLoading`, `isRefreshing`, `isOffline`, `errorMessage` y `hasLoaded`; exponer `refresh()` para swipe/acción de reintento y llamar `clearError()` al consumir el mensaje.

Reemplazar promociones de ejemplo por `state.promotions`; ante lista vacía mostrar estado vacío, y si `isOffline` mostrar una indicación discreta. Con caché disponible, el feed offline sigue teniendo contenido. Con caché vacía y fallo de red, mostrar el error y reintentar mediante `loadData()` o `refresh()`. No tocar datos de identidad ni condicionar las promociones a que exista sesión: el feed es público.

`selectedCategory` y `CatalogItem` no representan filtros de promociones: la tabla real `public.promociones` no tiene campo de categoría. Mantener oculto el selector de menú en esta fase, o usarlo solo cuando el negocio apruebe contenido estático; no filtrar promociones por categorías de producto.
