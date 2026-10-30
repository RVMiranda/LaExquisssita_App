# Protocolo de Colaboración Multi-Agente (Antigravity & Codex)

> **Proyecto:** La Exquisssita (App para Clientes)  
> **Ubicación:** `C:\Users\MACHENIKE\AndroidStudioProjects\LaExquisssita`  
> **Directorio de trabajo colaborativo:** `docs/`  

---

## 1. Principio Fundamental: Delimitación de Responsabilidades (Zero-Overlap)

Para garantizar un desarrollo ágil, libre de colisiones de código y con calidad de grado de producción:
* **Ningún agente modifica archivos asignados al otro agente de forma simultánea.**
* La integración entre la capa de presentación (UI) y la capa de datos (Backend/Supabase) se realiza **exclusivamente a través de contratos de Dominio** (interfaces en `domain/repository/` y modelos inmutables en `domain/model/`).

```
┌─────────────────────────────────────────────────────────────┐
│                         ANTIGRAVITY                         │
│  - Jetpack Compose, Material 3, Navigation & Shell          │
│  - Maquetación de pantallas (Novedades, Lealtad, Perfil)    │
│  - Bottom Navigation Bar y TopBar con estilo de marca       │
│  - Renderizado visual del Código QR (ZXing to ImageBitmap)  │
│  - Paleta Pantone, diseño Flat, tipografía sin emojis       │
│  - Componentes reusables (Botones, Tarjetas, Diálogos)      │
└──────────────────────────────┬──────────────────────────────┘
                               │ Consume interfaces de dominio
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                            CODEX                            │
│  - Data Layer, Supabase-kt (`auth-kt`, `postgrest-kt`)      │
│  - Google Credential Manager (Integración 1-Tap OAuth)      │
│  - DTOs, Mapeadores y Repositorios con ResultState          │
│  - Llamadas a RPCs (`ensure_customer_profile`, etc.)        │
│  - Inyección de dependencias y configuración                 │
│  - Persistencia de sesión y pruebas unitarias de datos      │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Matriz de Asignación de Archivos y Paquetes

| Módulo / Capa | Paquete / Ruta | Agente Responsable | Restricción |
| :--- | :--- | :--- | :--- |
| **Tokens & Tema** | `ui/theme/*` | **Antigravity** | Paleta oficial Pantone, tipografía jerárquica |
| **Componentes UI** | `core/designsystem/*` | **Antigravity** | Sin elevación, bordes 1.dp, cero emojis |
| **Navegación & Shell** | `navigation/*`, `MainActivity.kt` | **Antigravity** | BottomBar (3 pestañas), manejo de Scaffold |
| **Pantallas (Vistas)** | `features/**/ui/*Screen.kt` | **Antigravity** | Stateless, reciben UiState y emiten eventos |
| **ViewModels & State** | `features/**/viewmodel/*` | **Codex** | Propietario exclusivo de ViewModels, estados StateFlow y eventos; Antigravity los consume |
| **Modelos de Dominio** | `domain/model/*` | **Codex** | Data classes inmutables compartidas |
| **Contratos Repositorio**| `domain/repository/*` | **Codex** | Interfaces que Antigravity consume |
| **Supabase & PostgREST**| `data/remote/*` | **Codex** | DTOs y llamadas HTTP/RPC seguras |
| **Google Auth Client** | `data/auth/*` | **Codex** | Credential Manager API |
| **Inyección Hilt** | `core/di/*`, `di/*` | **Codex** | Proveedores de dependencias singleton |

---

## 3. Reglas de Calidad y Estilo Estricto

### 3.1 Estética Visual
1. **Paleta Oficial Única:**
   * `VanillaCustard` (`#FAF3E0`) para fondos de pantalla y superficies.
   * `BrandyBrown` (`#7E392F`) para acciones principales, acentos y bordes.
   * `ChocolateFondant` (`#3D2723`) para tipografía y textos.
   * `ApricotIllusion` (`#E5C2A4`) para divisores y bordes sutiles.
   * `OfflineAmber` (`#A07324`) para insignias de puntos y recompensas.
   * `ErrorRed` (`#B3261E`) para errores.
2. **Cero Emojis:** Prohibido el uso de emojis en cualquier texto o botón. La identidad visual se expresa mediante iconos vectoriales outline y buena tipografía.
3. **Estilo Flat Premium:** Tarjetas con elevación `0.dp`, bordes nítidos de `1.dp` y radios de esquina consistentes (`16.dp` para tarjetas, `12.dp` para botones).

### 3.2 Seguridad y Manejo de Datos
1. **Clave Anon Únicamente:** Solo usar `SUPABASE_PUBLISHABLE_KEY` (anon). Prohibido cualquier acceso con privilegios elevados.
2. **Rol Cliente:** La app asume el rol estándar de cliente autenticado. No intenta consultar tablas reservadas para staff (`empleados`, `historial_pagos_empleados`, etc.).
3. **Manejo de Errores Silencioso y Amigable:** Si no hay conexión o falla una consulta, la UI debe mostrar estados limpios con reintento, sin volcar stack traces o códigos crudos al cliente.

## 4. Acuerdos vigentes — 18 de octubre de 2026

- Codex tiene escritura exclusiva sobre ViewModels, estados, eventos, contratos, datos/backend, configuración de dependencias, Gradle, manifiesto y reglas de respaldo.
- Antigravity tiene escritura exclusiva sobre pantallas, integración visual del shell, navegación, tema, componentes, QRGenerator y GallerySaver.
- Codex publica los contratos de presentación antes de que Antigravity conecte las pantallas. Las pantallas consumen estado y emiten eventos; no duplican la lógica de repositorios.
- Antigravity solicita a Codex cambios de dependencias/permisos. Toda reasignación de archivos se registra antes de editar.
- La base usa AppContainer y SharedPreferences. Hilt/DataStore son opciones de evolución, no una migración obligatoria del sprint inicial.
- La tarjeta debe recuperarse con el mismo QR entre instalaciones y dispositivos. No usar rotación como recuperación de caché.
- Migraciones aún no desplegadas; Google habilitado en Supabase, integración Android pendiente.
- Invitados: promociones generales. Menú opcional estático. Wallet fuera del primer sprint.

La asignación detallada y criterios de aceptación están en [Sprint 01](agents-cowork/sprint-01-cargas-de-trabajo.md). Ambos agentes deben consultarlos antes de comenzar; esta actualización no constituye una notificación externa ni un despliegue.
