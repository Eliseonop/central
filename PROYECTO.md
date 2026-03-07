# TCONTUR Central – Documentación del Proyecto

> **Última actualización:** 2026-03-06
> **Plataforma:** Kotlin Multiplatform (Android + iOS)
> **Package:** `com.tcontur.central`
> **Path:** `C:\Users\edu\Desktop\TCONTUR\android\central`

---

## Tabla de contenidos
1. [Stack tecnológico](#1-stack-tecnológico)
2. [Arquitectura](#2-arquitectura)
3. [Estructura de carpetas](#3-estructura-de-carpetas)
4. [Flujo completo de la aplicación](#4-flujo-completo-de-la-aplicación)
5. [Pantallas y navegación](#5-pantallas-y-navegación)
6. [Servicios en background](#6-servicios-en-background)
7. [API Endpoints](#7-api-endpoints)
8. [Almacenamiento local](#8-almacenamiento-local)
9. [Protocolo WebSocket](#9-protocolo-websocket)
10. [Permisos requeridos](#10-permisos-requeridos)
11. [Estado del proyecto](#11-estado-del-proyecto)

---

## 1. Stack tecnológico

| Librería | Versión | Uso |
|---|---|---|
| Kotlin | 2.3.0 | Lenguaje base |
| Compose Multiplatform | 1.10.0 | UI declarativa |
| Navigation Compose | jetbrains | Navegación tipada |
| Koin | 4.0.4 | Inyección de dependencias |
| Ktor | 3.1.3 | HTTP client |
| kotlinx-serialization | 1.7.3 | JSON |
| multiplatform-settings | 1.2.0 | Almacenamiento clave-valor |
| kotlinx-datetime | 0.6.2 | Fechas/horas en commonMain |
| Google Play Services (FusedLocation) | — | GPS Android |
| CLLocationManager | — | GPS iOS |

---

## 2. Arquitectura

```
Clean Architecture  →  Data → Domain → Presentation (MVI-style)
```

### Capas
```
┌─────────────────────────────────────────────────┐
│  Presentation  (Screen + ViewModel + UiState)   │
├─────────────────────────────────────────────────┤
│  Domain        (Entities + Repository interfaces│
│                + Use cases)                     │
├─────────────────────────────────────────────────┤
│  Data          (DTOs + Ktor services + Repo impl│
│                + AppStorage)                    │
├─────────────────────────────────────────────────┤
│  Core          (Nav, DI, Network, Socket,       │
│                Location, Permissions, Utils)    │
└─────────────────────────────────────────────────┘
```

### Patrón de estado (MVI)
- **UiState** — `data class` con `StateFlow` → expuesto al Screen
- **Events** — `sealed class` con `StateFlow<Event?>` → eventos one-shot (navegación, errores)
- **Actions** — funciones del ViewModel invocadas desde el Screen

---

## 3. Estructura de carpetas

```
composeApp/src/
│
├── commonMain/kotlin/com/tcontur/central/
│   ├── App.kt                          # Entry point Compose
│   ├── QrDataHolder.kt
│   │
│   ├── core/
│   │   ├── di/         AppModule.kt
│   │   ├── nav/        AppDestinations.kt · AppNavHost.kt
│   │   ├── network/    ApiResult.kt · HttpClientFactory.kt · SessionEventBus.kt
│   │   ├── permission/ RememberStartupPermissions.kt · StartupPermissionsScreen.kt
│   │   ├── socket/     ProtoSocketManager.kt · SocketEvent.kt · SocketServiceManager.kt
│   │   ├── storage/    AppStorage.kt · StorageKeys.kt
│   │   ├── location/   LocationData.kt · LocationManager.kt · LocationRepository.kt
│   │   └── utils/      DateUtils.kt
│   │
│   ├── data/
│   │   ├── model/      (DTOs: UserDto, EmpresaDto, InspeccionDto, …)
│   │   ├── AuthApiService.kt · AuthRepositoryImpl.kt
│   │   ├── EmpresaApiService.kt
│   │   ├── InspeccionApiService.kt
│   │   └── LocationApiService.kt
│   │
│   ├── domain/
│   │   └── inspectoria/ InspeccionDomain.kt
│   │
│   ├── splash/         SplashScreen.kt · SplashViewModel.kt
│   ├── login/          LoginScreen.kt · LoginViewModel.kt
│   │
│   ├── inspectoria/
│   │   ├── loading/    SocketLoadingScreen.kt · SocketLoadingViewModel.kt
│   │   ├── dashboard/  InspectoriaDashboardScreen.kt · InspectoriaDashboardViewModel.kt
│   │   ├── iniciar/    IniciarInspeccionScreen.kt · IniciarInspeccionViewModel.kt
│   │   ├── inspeccion/ InspeccionScreen.kt · InspeccionViewModel.kt
│   │   │   └── tabs/  CortesTab · CobrosTab · OcurrenciasTab · (Reintegros/Pasajeros)
│   │   ├── permission/ LocationServiceScreen.kt
│   │   └── nav/        InspectoriaNavGraph.kt · InspectoriaDrawer.kt
│   │
│   └── ui/
│       ├── theme/      AppTheme.kt · Colors.kt · Type.kt
│       └── components/ AppButton · AppTextField · LoadingOverlay
│
├── androidMain/kotlin/com/tcontur/central/
│   ├── MainActivity.kt · TconturApplication.kt
│   ├── core/di/        AndroidModule.kt
│   ├── core/location/  AndroidLocationManager.kt
│   │   └── background/ LocationForegroundService.kt
│   ├── core/services/  SocketService.kt
│   ├── core/socket/    AndroidSocketServiceManager.kt
│   ├── core/permission/ RememberStartupPermissions.android.kt
│   │                    RememberRequestLocationPermission.android.kt
│   └── core/utils/     DateUtils.android.kt · QrScannerView.android.kt
│
└── iosMain/kotlin/com/tcontur/central/
    ├── MainViewController.kt
    ├── core/di/        IosModule.kt
    ├── core/location/  IosLocationManager.kt
    ├── core/socket/    IosSocketServiceManager.kt
    ├── core/permission/ RememberStartupPermissions.ios.kt
    └── core/utils/     DateUtils.ios.kt · QrScannerView.ios.kt
```

---

## 4. Flujo completo de la aplicación

```
┌─────────────────────────────────────────────────────────────────────┐
│  APP START                                                          │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  StartupPermissionsScreen                                           │
│  • Verifica POST_NOTIFICATIONS + ACCESS_FINE/COARSE_LOCATION        │
│  • Si ya están concedidos → auto-skip (LaunchedEffect)              │
│  • Si faltan → muestra UI con botón "Conceder permisos"             │
└────────────────────────────┬────────────────────────────────────────┘
                             │  allGranted = true
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  SplashScreen (1.8s)                                                │
│  SplashViewModel:                                                   │
│  • Busca usuario almacenado (AppStorage → USER_JSON)                │
│  • Si existe → emite Authenticated(role)                            │
│  • Si no existe → emite Unauthenticated                             │
└──────────┬──────────────────────────────────────┬───────────────────┘
           │ Unauthenticated                      │ Authenticated(role)
           ▼                                      ▼
┌─────────────────────────┐          ┌──────────────────────────────────┐
│  LoginScreen            │          │  SocketLoading(role)             │
│  LoginViewModel:        │          │  (salta el login, va directo     │
│  • Carga lista empresas │          │   a conectar el socket)          │
│  • Valida empresa +     │          └───────────────┬──────────────────┘
│    usuario + password   │                          │
│  • POST /api/token-auth │                          │
│  • Guarda USER_JSON,    │                          │
│    TOKEN, EMPRESA_CODE, │                          │
│    EMPRESA_ID           │                          │
└──────────┬──────────────┘                          │
           │ Login OK                                │
           ▼                                         │
┌─────────────────────────────────────────────────────────────────────┐
│  SocketLoadingScreen                                                │
│  SocketLoadingViewModel — 3 pasos:                                  │
│                                                                     │
│  1. fetchEmpresaAndConnect()                                        │
│     • Lee EMPRESA_ID del storage                                    │
│     • GET /tracker/empresas/{id} → obtiene IP del servidor (compute)│
│     • Construye wsUrl:                                              │
│       - Con IP: ws://{ip}:22222?tipo=I                              │
│       - Fallback: wss://{empresaCodigo}-23lnu3rcea-uc.a.run.app/ws  │
│     • socketServiceManager.startLocationTracking() → inicia         │
│       LocationForegroundService                                     │
│     • socketServiceManager.connect(wsUrl)                           │
│                                                                     │
│  2. observeSocketConnection()                                       │
│     • Espera protoSocketManager.isConnected = true                  │
│     • UI: "Logueando..."                                            │
│     • Envía frame WS: { id, code } con formatKey="login"           │
│                                                                     │
│  3. observeLoginConfirmation()                                      │
│     • Espera socketEvent: MessageDecoded(header="login")            │
│     • protoSocketManager.setAuthenticated(true)                     │
│     • UI: "¡Conexión exitosa!" (600ms) → navega a Home             │
└────────────────────────────┬────────────────────────────────────────┘
                             │ NavigateToHome
                             ▼
              ┌──────────────────────────────┐
              │  UserRole.fromCargo(cargo)   │
              ├──────────────────────────────┤
              │  "I" → InspectoriaRoot       │
              │  "C" → ConductorRoot (stub)  │
              │  "A" → AdminRoot (stub)      │
              └──────────────┬───────────────┘
                             │ (role = Inspectoria)
                             ▼
```

---

### Flujo Inspectoria (rol activo)

```
┌─────────────────────────────────────────────────────────────────────┐
│  InspectoriaDashboard                                               │
│  InspectoriaDashboardViewModel:                                     │
│  • Carga usuario almacenado                                         │
│  • GET /api/inspecciones/ver/ → inspección pendiente                │
│  • GET /api/inspecciones/?inspector={id}&dia={fecha} → lista del día│
│  • GET /api/inspecciones/resumen/?dia={fecha} → totales             │
│  • Calcula: total pasajeros, reintegros, última bajada              │
│                                                                     │
│  UI Cards:                                                          │
│  ┌──────────────┬──────────────┐                                    │
│  │ Inspecciones │ Pasajeros    │  ← azul, rojo                     │
│  ├──────────────┼──────────────┤                                    │
│  │ Reintegros   │ Última Bajada│  ← ámbar, verde                   │
│  └──────────────┴──────────────┘                                    │
│                                                                     │
│  Botón principal:                                                   │
│  • Sin inspección pendiente → "Inspeccionar" → IniciarInspeccion   │
│  • Con inspección pendiente → "Continuar inspección" →             │
│                                InspeccionActiva(id)                 │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
          ┌────────────────┴────────────────┐
          │ "Inspeccionar"                  │ "Continuar"
          ▼                                 ▼
┌───────────────────────────┐   ┌───────────────────────────────────┐
│  IniciarInspeccionScreen  │   │  InspeccionScreen(id)             │
│                           │   │  (ver sección abajo)              │
│  3 tabs:                  │   └───────────────────────────────────┘
│  ┌──────────────────────┐ │
│  │ Tab 1: Formulario    │ │
│  │ • Dropdown unidades  │ │
│  │ • Validación GPS     │ │
│  │   (CHECKING/VALID/   │ │
│  │    INVALID)          │ │
│  │ • Checkbox ticketera │ │
│  ├──────────────────────┤ │
│  │ Tab 2: QR            │ │
│  │ • Cámara escáner     │ │
│  │ • Parsea QR → muestra│ │
│  │   unidad, subida,    │ │
│  │   salida, cortes     │ │
│  ├──────────────────────┤ │
│  │ Tab 3: Mapa          │ │
│  │ • Próximamente       │ │
│  └──────────────────────┘ │
│                           │
│  Botón "Crear Inspección" │
│  POST /api/inspecciones/  │
│  iniciar/                 │
│  → InspeccionActiva(id)   │
└───────────────────────────┘
```

---

### Flujo InspeccionActiva

```
┌─────────────────────────────────────────────────────────────────────┐
│  InspeccionScreen(id)                                               │
│  TopBar: "Insp. PAD {padron} ({placa})"                            │
│                                                                     │
│  InspeccionViewModel:                                               │
│  • GET /api/inspecciones/{id}/ → carga inspección                  │
│  • GET /api/inspecciones/{id}/suministros/ → cortes/tickets        │
│  • (Si viene de QR → usa datos del QrDataHolder)                   │
│                                                                     │
│  3 tabs:                                                            │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │ Tab 1: CORTES                                                 │  │
│  │ • Lista de tickets con número inicial/final                   │  │
│  │ • Botón "Terminar" / "Reestablecer" por ticket               │  │
│  ├───────────────────────────────────────────────────────────────┤  │
│  │ Tab 2: COBROS (split en Reintegros / Pasajeros)              │  │
│  │ • Reintegros: botones +/− con monto total                    │  │
│  │ • Pasajeros: botones +/− con monto total                     │  │
│  ├───────────────────────────────────────────────────────────────┤  │
│  │ Tab 3: OCURRENCIAS                                           │  │
│  │ • Form: motivo + cargo (conductor/cobrador)                  │  │
│  │ • Lista de ocurrencias registradas con botón eliminar        │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  BottomBar:                                                         │
│  • Chips: [🔄 {n} reintegros · S/. {monto}] [👤 {n} pas · S/. x]  │
│  • [Cancelar] ────────────────── [✓ Finalizar]                     │
│                                                                     │
│  Cancelar → Dialog: ingresa motivo → POST .../cancelar/            │
│  Finalizar → Dialog: confirmar → PUT .../finalizar/ (payload      │
│              completo: cortes, reintegros, pasajeros, ocurrencias, │
│              GPS inicio/fin, timestamps)                            │
│           → back a Dashboard                                        │
└─────────────────────────────────────────────────────────────────────┘
```

---

### Flujo background (GPS + Socket)

```
  App Start
      │
      ▼
  SocketLoadingViewModel.fetchEmpresaAndConnect()
      │
      ├─→ socketServiceManager.startLocationTracking()
      │       │
      │       └─→ (Android) startForegroundService(LocationForegroundService)
      │
      └─→ socketServiceManager.connect(wsUrl)
              │
              └─→ (Android) SocketService — OkHttp WS abierto

  LocationForegroundService — runStartupSequence():
  ┌─────────────────────────────────────────────────────┐
  │ [0] ¿USER_JSON en storage?  No → stopSelf()         │
  │ [1] Espera isConnected=true (timeout 30s)           │
  │     Notificación: 🔴 Iniciando conexión...          │
  │ [2] Espera isAuthenticated=true (timeout 30s)       │
  │     Notificación: 🟡 Socket conectado · Logueando..│
  │ [3] Logueado ✓                                      │
  │     Notificación: 🟢 Logueado ✓                     │
  │ [4] startLocationUpdates() (FusedLocationProvider)  │
  │     • Intervalo: 2500ms                             │
  │     • Precisión mínima: 50m                         │
  │     • Envío al socket: cada 5000ms                  │
  │     • Payload: { time: LocalDateTime, lat, lng }    │
  │     • formatKey: "position"                         │
  │     Notificación: 🟢 Conectado [lat, lng]           │
  └─────────────────────────────────────────────────────┘

  ProtoSocketManager — fuente de verdad del socket:
  • isConnected: StateFlow<Boolean>
  • isAuthenticated: StateFlow<Boolean>  ← se resetea si desconecta
  • socketEvents: SharedFlow<SocketEvent>
```

---

## 5. Pantallas y navegación

### Destinos (`AppDestinations.kt`)

| Destination | Tipo | Descripción |
|---|---|---|
| `StartupPermissions` | `object` | Gate de permisos OS — primera pantalla |
| `Splash` | `object` | Verificación de sesión almacenada |
| `Login` | `object` | Formulario de autenticación |
| `SocketLoading(role)` | `data class` | Conexión WebSocket + auth de socket |
| `InspectoriaRoot` | `object` | NavGraph del rol Inspectoría |
| `ConductorRoot` | `object` | Placeholder |
| `AdminRoot` | `object` | Placeholder |
| `InspectoriaDashboard` | `object` | Home del inspector |
| `IniciarInspeccion` | `object` | Crear nueva inspección |
| `InspeccionActiva(id)` | `data class` | Editar inspección activa |
| `LocationPermission` | `object` | Solicitud de permiso GPS en runtime |
| `LocationService` | `object` | Pantalla para activar el servicio de ubicación OS |

### SessionEventBus (manejo 401)
Cuando cualquier API retorna 401 → `SessionEventBus.emitUnauthorized()` → `AppNavHost` navega a `Login` limpiando el backstack completo.

---

## 6. Servicios en background

### LocationForegroundService (Android)
- **CHANNEL_ID:** `"TID"`
- **NOTIFICATION_ID:** `112233`
- **foregroundServiceType:** `location`
- **START_STICKY:** se reinicia si el OS lo mata; aplica credential guard para no quedar colgado
- Inyecta via Koin: `LocationRepository`, `ProtoSocketManager`, `AppStorage`
- Único dueño del contenido de la notificación persistente

### SocketService (Android)
- Maneja la conexión OkHttp WebSocket
- Comparte `CHANNEL_ID="TID"` y `NOTIFICATION_ID=112233` con `LocationForegroundService`
- `updateNotification()` es no-op (LocationForegroundService controla el contenido)
- Decodifica mensajes con `Protocol.decode()` → llama `ProtoSocketManager.emitMessageDecoded(header, data)`

### Notificación unificada
Ambos servicios llaman `startForeground()` con el mismo ID → Android muestra **una sola notificación**.

---

## 7. API Endpoints

### Base URLs
```
API_URBANITO  = https://urbanito-23lnu3rcea-uc.a.run.app
API_EMPRESA   = https://{empresaCodigo}-23lnu3rcea-uc.a.run.app
WS_EMPRESA_IP = ws://{compute_ip}:22222?tipo=I
WS_FALLBACK   = wss://{empresaCodigo}-23lnu3rcea-uc.a.run.app/ws
```

### Auth
| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `{API_EMPRESA}/api/token-auth` | Login → token + datos de usuario |
| `GET` | `{API_URBANITO}/tracker/empresas/{id}` | Empresa por ID (obtiene IP compute) |

### Inspecciones
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/inspecciones/ver/` | Inspección activa del inspector |
| `GET` | `/api/inspecciones/?inspector={id}&dia={date}` | Lista del día |
| `GET` | `/api/inspecciones/resumen/?dia={date}` | Resumen diario (totales) |
| `POST` | `/api/inspecciones/iniciar/` | Crear nueva inspección |
| `POST` | `/api/inspecciones/{id}/cancelar/` | Cancelar con motivo |
| `PUT` | `/api/inspecciones/{id}/finalizar/` | Finalizar con datos completos |
| `GET` | `/api/inspecciones/{id}/suministros/` | Stock de tickets de la unidad |

### Unidades
| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/unidades/` | Lista de buses disponibles |
| `GET` | `/api/unidades/{id}/cercania/?lat=&lng=` | Verificar proximidad GPS |

### WebSocket (mensajes outgoing)
| formatKey | Datos | Descripción |
|---|---|---|
| `"login"` | `{ id, code }` | Autenticación de socket |
| `"position"` | `{ time: LocalDateTime, latitude, longitude }` | Posición GPS cada 5s |

### WebSocket (mensajes incoming)
| header | Descripción |
|---|---|
| `"login"` | Confirmación de login por el servidor |

---

## 8. Almacenamiento local

```kotlin
object StorageKeys {
    const val USER_JSON    = "user_json"      // Usuario serializado completo
    const val AUTH_TOKEN   = "auth_token"     // Bearer token API
    const val EMPRESA_CODE = "empresa_code"   // Código para construir URLs
    const val EMPRESA_ID   = "empresa_id"     // ID numérico de la empresa
    // + credenciales remember-me
}
```

Implementación: `multiplatform-settings` (SharedPreferences en Android, NSUserDefaults en iOS).

---

## 9. Protocolo WebSocket

- **Librería:** Protocol (binario personalizado)
- **`Protocol.decode()`** retorna el **nombre del formato** (`"login"`, `"position"`) — NO el carácter binario del header
- **`Protocol.encode(formatKey, data)`** serializa para envío
- Los datos de tipo `datetime` en el schema esperan `java.time.LocalDateTime` (no `Long`)

---

## 10. Permisos requeridos

### Android (`AndroidManifest.xml`)
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />  <!-- API 33+ -->
```

### Runtime (solicitados en `StartupPermissionsScreen`)
- `POST_NOTIFICATIONS` (Android 13+ / API 33+)
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

### iOS (`Info.plist`)
```xml
NSLocationWhenInUseUsageDescription
NSLocationAlwaysAndWhenInUseUsageDescription
NSCameraUsageDescription
```

---

## 11. Estado del proyecto

### ✅ Implementado y funcional
- [x] Gate de permisos antes del login (`StartupPermissionsScreen`)
- [x] Splash con detección de sesión almacenada
- [x] Login con selección de empresa + remember me
- [x] Conexión WebSocket con flujo de auth correcto
- [x] `ProtoSocketManager.isAuthenticated` StateFlow como fuente de verdad
- [x] `LocationForegroundService` con credential guard + timeouts 30s
- [x] GPS tracking (FusedLocationProvider, 2500ms, mín. precisión 50m)
- [x] Envío de posición cada 5s con `LocalDateTime` (POST_NOTIFICATIONS + WS)
- [x] Notificación unificada (un único NOTIFICATION_ID=112233)
- [x] Dashboard con estadísticas del día
- [x] Crear inspección (Formulario + QR + validación GPS)
- [x] Inspección activa con tabs: Cortes, Cobros, Ocurrencias
- [x] Cancelar / Finalizar inspección
- [x] Logs descriptivos en cada paso (`TCONTUR_GPS`, `TCONTUR_PERMS`, etc.)
- [x] Eliminación completa del código WebView legacy

### 🔲 Pendiente / Placeholders
- [ ] `ConductorRoot` — pantallas del conductor
- [ ] `AdminRoot` — pantallas del administrador
- [ ] Tab "Mapa" en `IniciarInspeccion` — "Próximamente"
- [ ] iOS background location (CLLocationManager continuous tracking)
- [ ] Manejo de reconexión automática del WebSocket
- [ ] Tests unitarios (ViewModels + Repository)

---

### Gotchas KMP importantes

```kotlin
// ❌ FALLA en K2 commonMain:
Clock.System.now()

// ✅ Usar expect/actual:
expect fun currentDateStr(): String

// ❌ FALLA en commonMain:
"%.2f".format(value)

// ✅ Usar:
expect fun Double.toDecimalStr(): String

// ❌ FALLA en commonMain:
String.format("...", args)

// ✅ Evitar en commonMain, usar actual en cada plataforma
```
