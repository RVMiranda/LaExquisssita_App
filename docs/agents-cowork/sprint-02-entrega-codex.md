# Entrega Codex — Sprint 02: promociones y estado de Inicio

> 19 de octubre de 2026. Capa de promociones y `HomeViewModel` implementados; no se modificaron los archivos Compose asignados a Antigravity.

## Integración para Antigravity

Crear/compartir una instancia en el shell de la app, igual que `CustomerViewModel`:

```kotlin
val app = LocalContext.current.applicationContext as LaExquisssitaApp
val home: HomeViewModel = viewModel(factory = app.container.homeViewModelFactory)
val state by home.state.collectAsStateWithLifecycle()
```

Mapeo `Promotion` → `PromotionItemUi`:

| UI | Dominio | Regla |
| --- | --- | --- |
| `id` | `id` | Identificador estable |
| `title` | `titulo` | Texto del backend |
| `description` | `descripcion` | Texto del backend |
| `terms` | `terminosCondiciones` | Puede ser null |
| `imageUrl` | `imagenUrl` | Puede ser null; Coil resuelve carga/placeholder |
| `validity` | `fechaVencimiento` | Formatear `yyyy-MM-dd`; null = “Vigencia sin fecha límite”. No usar `fechaPublicacion` como vencimiento. |
| `tag` | — | Usar etiqueta genérica; no hay categoría de promoción en el esquema actual. |

Usar `state.promotions` en lugar de los datos de promoción de ejemplo en `HomeScreen`. Con lista vacía y `hasLoaded`, mostrar estado vacío. `isLoading` cubre la primera carga; `isRefreshing` la actualización; `isOffline` indica que se muestra la caché. `errorMessage` se puede descartar con `home.clearError()`. Reintentar con `home.refresh()` o `home.loadData()`.

No condicionar el feed al inicio de sesión. Las promociones se consultan como datos públicos. El selector de categorías de menú y los platillos de demostración no tienen contrato/fuente en esta fase; ocultarlos. Se conserva `CatalogItem` solo como tipo para el futuro, sin consulta ni precios inventados.

## Implementación

- `Promotion` refleja las columnas del Manager: `id`, `titulo`, `descripcion`, `terminos_condiciones`, `imagen_url`, `fecha_publicacion`, `fecha_vencimiento`, `activo`.
- `SupabasePromotionRemote` hace SELECT explícito de columnas de `public.promociones`, filtra `activo = true`, ordena por fecha de publicación descendente y limita a 100.
- El repositorio excluye promociones vencidas usando la fecha local del dispositivo. La fecha de vencimiento es inclusiva durante ese día. Guarda la lista vigente cifrada con AES-GCM/Android Keystore; si falla la red, vuelve a filtrar la caché para no mostrar promociones vencidas. Error de caché al guardar no descarta una respuesta de red exitosa.
- Si no hay caché y la consulta falla, el resultado es `Result.failure`; el ViewModel publica un error recuperable.
- `AppContainer.homeViewModelFactory` está listo. `MainScreen` y `HomeScreen` siguen siendo propiedad de Antigravity para evitar colisión.
- No se hicieron migraciones ni cambios remotos de Supabase. El contrato coincide con `ExquisssitaAppManager/docs/backend-contract.md` y las migraciones versionadas del Manager.

## Verificación

Se agregaron pruebas unitarias para filtro/orden/cache remoto, fallback offline con vencimiento, propagación de cancelación y nombres de columnas serializados. `:app:testDebugUnitTest` y `:app:assembleDebug` terminaron con **BUILD SUCCESSFUL**. El único warning de Kotlin es una API de color deprecated preexistente en `MainScreen`; no pertenece a esta carga.
