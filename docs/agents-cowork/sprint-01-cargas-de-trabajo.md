# Asignación de Cargas de Trabajo — Sprint 01

> Acuerdos confirmados por el propietario el 18 de octubre de 2026.
> Objetivo: acceso con Google, teléfono opcional, tarjeta real permanente por cliente, disponibilidad offline y descarga en galería.

## Contexto obligatorio para ambos agentes

- Google Sign-In está habilitado en Supabase con sus credenciales, según el propietario. Falta conectar y verificar Android; `webClientId` sigue vacío en el código revisado.
- Las migraciones del backend de clientes NO se han desplegado. Los contratos documentados son objetivos, no el estado remoto verificado.
- El QR debe ser el mismo tras cerrar sesión, reinstalar o acceder desde otro teléfono. La caché local no puede ser su única fuente.
- `rotate_customer_qr_token` invalida el código anterior: NO usarla para recuperar una caché ausente. Codex debe definir recuperación remota idempotente antes de conectar la tarjeta real.
- Puntos y visitas se actualizan con conexión sin modificar el QR. Una instalación nueva requiere conexión inicial para recuperar la tarjeta; después debe permitir mostrarla offline.
- Invitados ven promociones generales. El menú es opcional y puede ser contenido estático validado por el negocio, sin consultar inventario.
- Google Wallet queda fuera del sprint. No mostrar una acción activa sin implementación.

## Escritura exclusiva

Rutas Kotlin relativas a `com.mirandadevsource.laexquisssita`.

| Archivos o responsabilidad | Propietario |
| --- | --- |
| `domain/model/*`, `domain/repository/*` | Codex |
| `data/**`, `core/config/*`, `core/network/*`, `core/security/*` | Codex |
| `core/di/*`, `di/*`, `LaExquisssitaApp.kt` | Codex |
| `features/**/viewmodel/*`, estados y eventos de presentación | Codex |
| Gradle, catálogo de versiones, dependencias, manifiesto y reglas de respaldo | Codex |
| `ui/theme/*`, `core/designsystem/*` | Antigravity |
| `features/**/ui/*`, `navigation/*`, `ui/MainScreen.kt`, `MainActivity.kt` | Antigravity |
| `core/util/QRGenerator.kt`, `core/util/GallerySaver.kt` | Antigravity |
| Contratos backend, diseño de migraciones y pruebas de datos/estado | Codex |

Antigravity solicita a Codex cambios de dependencias o permisos. Cualquier reasignación se registra antes de editar; nunca hay dos escritores simultáneos sobre el mismo archivo.

## Trabajo de Codex

1. Configurar Web Client ID y conectar Credential Manager con Supabase. Verificar acceso, cancelación, errores y restauración de sesión.
2. Publicar contratos de estados/eventos y ViewModels antes de la conexión de pantallas. Incluir inicialización, invitado, autenticado, carga de perfil y error/reintento.
3. Conectar `ensure_customer_profile` y teléfono opcional. Si falla el perfil tras autenticar, ofrecer reintento sin fingir éxito.
4. Diseñar emisión/recuperación del mismo QR para la misma identidad, incluyendo concurrencia entre dispositivos. Alinear las migraciones de Manager y su escáner antes del despliegue.
5. Implementar caché aislada por cliente, limpieza local al salir y recuperación del mismo token al volver a entrar. Cerrar sesión no invalida la tarjeta remota.
6. Exponer puntos, visitas, estado de actualización y preferencias persistentes de brillo.
7. Probar repositorios/estados relevantes y verificar compilación e integración.

## Trabajo de Antigravity

1. Conectar shell y pantallas a estados/eventos de Codex; retirar sesión simulada y datos de ejemplo del flujo real.
2. Implementar acceso desde Mi Tarjeta/Perfil y onboarding de teléfono opcional, validación de 10 dígitos y «Omitir por ahora».
3. Renderizar el token con `QRGenerator`, mostrar puntos/visitas y distinguir datos almacenados de datos actualizados. Refrescar datos no reemite el QR.
4. Conectar `GallerySaver` desde tarjeta/configuración. Confirmar únicamente guardados exitosos y mostrar errores recuperables.
5. Consumir la preferencia persistente de brillo y restaurar el brillo al abandonar la tarjeta.
6. Mantener Novedades accesible sin cuenta y centrada en promociones generales. No anunciar ofertas ficticias ni funciones de pedidos/notificaciones fuera del alcance.

## Criterios de aceptación conjuntos

- Inicio como invitado; promociones generales sin acceso a información operativa.
- Google Sign-In real, creación/recuperación del perfil y teléfono omitible.
- Mismo contenido QR tras reinicio, cierre de sesión, reinstalación y acceso desde otro dispositivo. El segundo dispositivo no invalida la imagen del primero.
- Clientes distintos no comparten caché ni código; cerrar sesión elimina el acceso local a información privada.
- Tarjeta previamente recuperada visible sin conexión. Reconectar actualiza puntos/visitas sin cambiar el QR.
- Imagen descargada legible por Manager; un guardado fallido no muestra éxito.
- Mostrar el QR offline no implica que el cajero pueda validar visitas/canjes offline.

## Dependencias pendientes

- Web Client ID en Android y prueba real de autenticación.
- Contrato de QR recuperable, migraciones probadas/desplegadas y permisos de promociones públicas verificados.
- Contratos Kotlin publicados por Codex para Antigravity.
- Compilación debug, pruebas unitarias y lint ya ejecutados; detalles en la entrega Codex. Pruebas SQL y Google real aún pendientes.

Actualización de implementación: consultar [entrega Codex](sprint-01-entrega-codex.md) para los contratos Kotlin ya disponibles, validaciones y bloqueos. Las pantallas siguen asignadas a Antigravity; la migración nueva aún no está desplegada.

