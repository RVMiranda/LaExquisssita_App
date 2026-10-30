# Asignación de Cargas de Trabajo — Sprint 04: Recompensas Semestrales, Historial en Vivo y Rediseño de Inicio

> **Proyecto:** La Exquisssita (App para Clientes)  
> **Ubicación:** `C:\Users\MACHENIKE\AndroidStudioProjects\LaExquisssita`  
> **Objetivo del Sprint:** 
> 1. Implementar la mecánica de recompensas y puntos con vigencia semestral (Enero-Junio y Julio-Diciembre, estilo Cinépolis) con regla estricta de canje único por periodo por usuario.
> 2. Conectar y renderizar el historial cronológico real de visitas y movimientos de puntos en el perfil del cliente.
> 3. Rediseñar la pantalla de inicio (`HomeScreen`): eliminar duplicados visuales de promociones, mantener el carrusel con acceso a "Ver todas", e incorporar dos nuevas mini-secciones: Información del Negocio (sucursal, horarios, contacto) y Enlace al Sitio Web Oficial.
> 4. Asegurar que Codex corrobore la consistencia entre migraciones locales y remotas mediante el CLI de Supabase.

---

## 1. Arquitectura y División de Responsabilidades (Zero-Overlap)

```
┌────────────────────────────────────────────────────────────────────────┐
│                   ANTIGRAVITY (UI & Experiencia)                       │
│  - HomeScreen: Carrusel de promociones sin duplicación vertical         │
│  - Hoja / Modal "Ver todas las promociones" para lectura detallada     │
│  - Mini-sección "Nuestra Sucursal" (Dirección, horarios, teléfonos)     │
│  - Mini-sección "Página Web Oficial" (Banner interactivo con Intent)   │
│  - LoyaltyScreen: Catálogo dinámico de recompensas semestrales         │
│    (Badges: Disponible / Canjeada en el periodo / Faltan X pts)         │
│  - ProfileScreen: Historial cronológico de puntos y visitas            │
│  - Estilo Flat Premium, paleta artesanal y Cero Emojis                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Consume estados StateFlow y emite eventos
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      CODEX (Data, Domain & VM)                         │
│  - Verificación CLI de Supabase (esquemas locales vs remotos)          │
│  - Soporte de periodos semestrales (Ene-Jun / Jul-Dic) en lógica de    │
│    recompensas y canjes únicos por periodo en `canjes_recompensas`     │
│  - Repositorio y cliente de historial real (`puntos_movimientos`)      │
│  - CustomerViewModel: exposición de `rewards` e `history` reactivos   │
│  - Pruebas unitarias de elegibilidad semestral y mapeos de datos       │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Reglas del Negocio para Recompensas Semestrales (Estilo Cinépolis)

1. **Vigencia Semestral de Puntos y Recompensas:**
   - **Periodo 1:** 1 de enero al 30 de junio.
   - **Periodo 2:** 1 de julio al 31 de diciembre.
   - La ventana de canje y validez de recompensas está delimitada por el semestre calendario actual.
2. **Canje Único por Recompensa por Semestre:**
   - Cada cliente únicamente puede canjear **una sola vez cada recompensa disponible durante el mismo periodo semestral**.
   - Si el cliente ya canjeó una recompensa específica (ej. *Café Americano o Capuccino*) dentro del semestre en curso (verificable en `public.canjes_recompensas`), su estado en la app debe cambiar a **Canjeada este periodo** y bloquear solicitudes adicionales de esa misma recompensa hasta el siguiente ciclo semestral.
   - Si no ha sido canjeada y el cliente tiene saldo suficiente (`puntos_lealtad >= costo_puntos`), su estado es **Disponible para canje**.
   - Si no alcanza el puntaje, se indica claramente el saldo faltante (ej. *Te faltan 15 pts*).
   - Confirmación del negocio (22 de octubre): el límite también se hace cumplir en `redeem_loyalty_reward` del Manager; la interfaz del cliente por sí sola no constituye bloqueo del canje en caja. Se conserva el resultado idempotente de una misma solicitud.

---

## 3. Tareas Asignadas a CODEX (Data, Backend & ViewModel)

### [CODEX-13] Auditoría y Verificación de Migraciones CLI
* **Responsabilidad:** Verificar el estado de las migraciones canónicas locales en `ExquisssitaAppManager/supabase/migrations` contrastadas con la base de datos remota mediante `npx supabase migration list`.
* **Criterio:** Confirmar que no existan discrepancias ni funciones pendientes (`ensure_customer_profile`, `get_or_create_customer_qr_token`, `recompensas`, `canjes_recompensas`, `puntos_movimientos`).

### [CODEX-14] Lógica Semestral y Validación de Canje Único
* **Archivos asignados:**
  * `domain/model/Reward.kt` (o modelo de dominio de recompensas)
  * `data/remote/SupabaseCustomerRemote.kt`
  * `data/repository/LoyaltyRepositoryImpl.kt`
* **Especificación:**
  * Determinar programáticamente el rango de fechas del periodo semestral activo según la fecha del sistema (`created_at >= inicioSemestre AND created_at <= finSemestre`).
  * Consultar los canjes del cliente autenticado en `public.canjes_recompensas` para identificar cuáles `recompensa_id` ya fueron consumidas en el semestre actual.
  * Extender o enriquecer el modelo de recompensa con `alreadyRedeemedInPeriod: Boolean` o crear un modelo enriquecido para presentación.

### [CODEX-15] Integración de Historial Real de Visitas y Puntos
* **Archivos asignados:**
  * `domain/model/PointMovement.kt`
  * `data/repository/LoyaltyRepositoryImpl.kt`
* **Especificación:**
  * Afinar `LoyaltyRepository.getPointsHistory()` para traer los movimientos reales de `public.puntos_movimientos` ordenados descendentemente por fecha (`created_at DESC`).
  * Validar mapeo de tipos (`visita`, `canje`, `apertura`, `ajuste`) y motivos legibles para el cliente.

### [CODEX-16] Exposición Reactiva en CustomerViewModel
* **Archivos asignados:**
  * `features/auth/viewmodel/CustomerViewModel.kt`
* **Especificación:**
  * Agregar a `CustomerUiState`:
    ```kotlin
    val rewards: List<CustomerRewardItem> = emptyList(),
    val pointsHistory: List<PointMovement> = emptyList(),
    val loadingRewards: Boolean = false,
    val loadingHistory: Boolean = false,
    val currentPeriodLabel: String = "" // Ej: "Periodo Ene - Jun 2026" o "Periodo Jul - Dic 2026"
    ```
  * Cargar automáticamente recompensas e historial tras autenticarse o en `refresh()`.
  * Manejo de errores amigable sin romper la sesión activa.

### [CODEX-17] Pruebas Unitarias de Reglas de Negocio
* **Archivos asignados:** `app/src/test/java/.../LoyaltyRepositoryTest.kt` o similar
* **Especificación:**
  * Pruebas de cálculo de semestres (enero-junio vs julio-diciembre).
  * Pruebas de bandera `alreadyRedeemedInPeriod` cuando existe un registro en `canjes_recompensas`.
  * Pruebas de ordenamiento del historial de puntos.

---

## 4. Tareas Asignadas a ANTIGRAVITY (UI, Presentación & Componentes)

### [ANTIGRAVITY-10] Rediseño de HomeScreen y Nuevas Secciones Informativas
* **Archivos asignados:** `features/home/ui/HomeScreen.kt`
* **Especificación:**
  * **Eliminar duplicidad:** Retirar la lista vertical inferior de promociones (`items(promotionItems)` en la columna principal).
  * **Carrusel con acceso ampliado:**
    * Mantener el carrusel horizontal superior para promociones destacadas.
    * Agregar cabecera con botón / icono outline ("Ver todas" con flecha $\rightarrow$) que abra una hoja modal con la vista completa y expandida de promociones vigentes.
  * **Mini-sección 1: Información del Negocio ("Nuestra Sucursal"):**
    * Tarjeta Flat (`WarmWhite`, borde `ApricotIllusion`, 0.dp elevación).
    * Dirección física de la sucursal (carnitas y tradición artesanal).
    * Horarios de atención (ej. Lunes a Domingo de 9:00 a 18:00 hrs).
    * Botón de contacto rápido (llamada telefónica o WhatsApp del negocio con `Intent.ACTION_VIEW`).
  * **Mini-sección 2: Sitio Web Oficial:**
    * Banner editorial estilizado con llamado a la acción para visitar la web oficial.
    * Al pulsar, ejecuta un `Intent(Intent.ACTION_VIEW)` con URL configurable (con fallback seguro).

### [ANTIGRAVITY-11] Catálogo Dinámico de Recompensas Semestrales en LoyaltyScreen
* **Archivos asignados:** `features/loyalty/ui/LoyaltyScreen.kt`
* **Especificación:**
  * Reemplazar los items estáticos por la lista reactiva proveniente de `CustomerViewModel`.
  * Banner informativo superior de periodo: *"Periodo de beneficios vigente: Julio - Diciembre 2026"*.
  * Tarjetas de recompensa con 3 estados visuales nítidos:
    1. **Disponible:** Botón o insignia dorada `OfflineAmber`, con acción para ver detalle.
    2. **Canjeada este periodo:** Insignia en gris neutro `TextMuted` con leyenda *"Canjeada en este semestre (1 de 1)"*.
    3. **Puntos insuficientes:** Insignia que indica *"Te faltan X pts"*.
  * Modal informativo de canje al tocar una recompensa disponible:
    * Indica que el canje se solicita directamente en caja presentando el código QR permanente.
    * Aclara la vigencia semestral y la política de canje único.

### [ANTIGRAVITY-12] Historial Cronológico de Movimientos en ProfileScreen
* **Archivos asignados:** `features/profile/ui/ProfileScreen.kt`
* **Especificación:**
  * Transformar la sección estática en una lista interactiva de transacciones reales.
  * Cada fila de movimiento incluye:
    * Icono vectorial outline según el tipo (visita/compra acumulada vs canje de recompensa).
    * Descripción clara (ej. *"Visita registrada en caja"* o *"Canje: Café Americano"*).
    * Fecha formateada de manera amigable.
    * Badge de puntos: verde/dorado `+10 pts` para acumulaciones, o `ErrorRed` `-50 pts` para canjes.
  * Estado vacío amigable cuando el cliente aún no tenga transacciones registradas.

---

## 5. Criterios de Aceptación del Sprint 04

1. En la pantalla de inicio, las promociones solo se listan en el carrusel superior, y el botón "Ver todas" permite explorarlas sin saturar la vista principal.
2. La pantalla de inicio contiene la información comercial de la sucursal (dirección, horario, contacto) y el banner interactivo hacia el sitio web oficial.
3. El catálogo de recompensas se obtiene dinámicamente de Supabase y refleja con precisión el estado del cliente: disponible, canjeada en el semestre activo (bloqueando repeticiones) o puntos insuficientes.
4. El historial en el perfil muestra los movimientos reales registrados en caja con sus respectivos deltas de puntos y fechas.
5. Se mantiene estricta observancia del diseño Flat Premium, paleta artesanal y la regla de **Cero Emojis**.
6. Todo el código compila con `BUILD SUCCESSFUL` y pasa las pruebas unitarias.
