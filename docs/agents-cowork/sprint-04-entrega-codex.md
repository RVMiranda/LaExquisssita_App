# Entrega Codex — Sprint 04: recompensas semestrales e historial

> 22 de octubre de 2026. CODEX-13 a CODEX-17 implementados. Tareas de presentación conservadas para Antigravity.

## Estado para integrar la UI

`CustomerViewModel` expone `state.rewards`, `state.pointsHistory`, `state.currentPeriodLabel`, `state.loadingRewards`, `state.loadingHistory`, `state.rewardsError` y `state.historyError`. Los datos se cargan tras autenticar y al llamar `refresh()`. Un error de lectura queda en su sección y conserva la sesión y el último resultado disponible.

Cada `CustomerRewardItem` contiene `reward`, `alreadyRedeemedInPeriod`, `pointsMissing` e `isAvailable`. La recompensa canjeada tiene prioridad visual sobre saldo insuficiente; si no está canjeada, `pointsMissing == 0` representa elegibilidad por saldo. El cliente solo presenta/consulta elegibilidad; el canje real se solicita en caja.

`PointMovement` conserva los campos del ledger y ofrece `kind` y `displayDescription` para clasificar `apertura`, `visita`, `canje` y `ajuste`. Las visitas de caja aparecen como movimientos positivos; los canjes incluyen el motivo `Canje: <recompensa>` generado por el Manager. El historial se pide en orden descendente y se vuelve a ordenar defensivamente en el repositorio.

El semestre usa la fecha de `America/Mexico_City`, en línea con la fecha de negocio de `registrar_visita_qr`: 1 de enero a 1 de julio (fin exclusivo) y 1 de julio a 1 de enero. `currentPeriodLabel` entrega `Periodo Ene - Jun AAAA` o `Periodo Jul - Dic AAAA`.

## Implementación

- CODEX-13: Supabase CLI comparó el historial canónico; las 30 migraciones remotas y locales existentes coincidían, incluida la migración QR. La consulta remota confirmó RPC de perfil/QR, tablas del ledger con RLS y lectura autenticada de sus columnas necesarias.
- CODEX-14: `RewardPeriod`, `CustomerRewardItem` y la lectura de `canjes_recompensas` filtran por ventana `[inicio, inicio del siguiente semestre)`. Por aclaración del negocio, también se agregó en el Manager `supabase/migrations/20261022190359_enforce_semester_reward_redemption.sql`; la RPC de caja conserva idempotencia y rechaza un segundo canje de la misma recompensa para ese cliente en el semestre CDMX activo. Una recompensa distinta sí se puede canjear una vez.
- CODEX-15: historial desde `puntos_movimientos`, filtrado al perfil del cliente, con orden `created_at DESC, id DESC`, límite de 50 y etiquetas legibles.
- CODEX-16: cargas paralelas y estado reactivo en `CustomerViewModel`; respuestas tardías de otra sesión se descartan.
- CODEX-17: pruebas para límites semestrales, etiquetas, canje previo, puntos faltantes, recompensa distinta y orden/etiquetas del historial.

## Verificación y estado remoto

- `:app:testDebugUnitTest` y `:app:assembleDebug`: **BUILD SUCCESSFUL**.
- Después de crear la migración Release 04, `supabase migration list --linked` muestra las 30 migraciones previas en ambos lados y `20261022190359` solo en local. El remoto sigue sin la regla nueva.
- `supabase test db --local supabase/tests/release04_semester_redemption.test.sql` no pudo conectarse: Docker no está activo (`ECONNREFUSED 127.0.0.1:54322`). La prueba SQL contiene cuatro aserciones para canje anterior, reintento idempotente, recompensa distinta y rechazo de repetición; requiere ejecutarse después de iniciar Docker y aplicar la cadena a una base local limpia.
- No se ejecutó `db push` remoto. Primero correr pgTAP y revisar advisors; después desplegar desde `ExquisssitaAppManager` y verificar que `redeem_loyalty_reward` exponga el nuevo error `reward_already_redeemed_this_period`.

## Contrato para Antigravity

- Catálogo: iterar `customerUi.rewards`; leer `item.reward.titulo`, `descripcion`, `costoPuntos`, `item.alreadyRedeemedInPeriod`, `item.pointsMissing` e `item.isAvailable`.
- Periodo: mostrar `customerUi.currentPeriodLabel`.
- Historial: iterar `customerUi.pointsHistory`, mostrar `movement.displayDescription`, `movement.createdAt` y `movement.puntosDelta`.
- Vincular indicadores de progreso a `loadingRewards` / `loadingHistory`; los errores se muestran desde `rewardsError` y `historyError`. El empty state debe distinguir una lista vacía sin error.

Los archivos Kotlin están en `domain/model/RewardPeriod.kt`, `domain/model/CustomerRewardItem.kt`, `domain/model/PointMovement.kt`, `data/remote/SupabaseCustomerRemote.kt`, `data/repository/LoyaltyRepositoryImpl.kt` y `features/auth/viewmodel/CustomerViewModel.kt`.
