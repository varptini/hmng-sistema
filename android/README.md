# HMNG Android App

Sistema de gestión de inventario hospitalario para Android. Arquitectura Clean + Compose + Hilt.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│  Compose Screens + HiltViewModels + StateFlow/UiState    │
│  (dashboard, insumos, pedidos, bitácora, notif, reportes)│
└─────────────────────────┬────────────────────────────────┘
                          │ uses
┌─────────────────────────▼────────────────────────────────┐
│                     Domain Layer                          │
│  Repository Interfaces · Domain Models · Use Cases       │
└──────────────┬────────────────────────┬──────────────────┘
               │ implements             │ implements
┌──────────────▼──────────┐  ┌─────────▼──────────────────┐
│     Data / Remote        │  │     Data / Local (Room)     │
│  Retrofit + OkHttp       │  │  Entities · DAOs · Mappers  │
│  HmngApiService (REST)   │  │  HmngDatabase (SQLite)      │
│  SocketManager (WS)      │  │  EncryptedSharedPrefs       │
└─────────────────────────┘  └────────────────────────────-┘
```

**Key packages:**

| Package | Purpose |
|---|---|
| `data/remote` | Retrofit API service, DTOs, AuthInterceptor |
| `data/local` | Room entities, DAOs, mappers |
| `data/socket` | Socket.IO real-time events (SocketManager) |
| `data/repository` | Concrete repository implementations |
| `data/session` | SessionManager (EncryptedSharedPreferences) |
| `data/sync` | WorkManager background sync (SyncWorker) |
| `domain/model` | Plain Kotlin data classes |
| `domain/repository` | Repository interfaces |
| `presentation` | Compose screens, HiltViewModels |
| `data/di` | Hilt modules (Network, Database, Repository) |

---

## Build & Run

### Requirements
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 35 (target) / SDK 26 (min — Android 8.0+)

### Steps

```bash
cd android
./gradlew assembleDebug          # build APK
./gradlew installDebug           # install to connected device/emulator
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

---

## Backend Configuration

The app points to a local backend server. Update the IP if needed:

**`app/build.gradle.kts`:**
```kotlin
buildConfigField("String", "API_URL", "\"http://192.168.0.4:4000/api\"")
buildConfigField("String", "SOCKET_URL", "\"http://192.168.0.4:4000\"")
```

The backend must expose:
- REST API on `/api/*` (Retrofit)
- Socket.IO server on port 4000 (socket.io-client 2.x)
- Auth via JWT in `Authorization: Bearer <token>` header and Socket.IO `auth.token`

---

## Key Features

### Offline-First
- All insumos, notificaciones, and pedidos are stored in Room (SQLite).
- Screens read from Room first and refresh from network in background.
- WorkManager (`SyncWorker`) runs periodic background sync every 15 minutes.
- Pending operations (create/update) are queued locally and flushed when online.

### Real-Time (Socket.IO)
- `SocketManager` connects on app start and listens for 4 events:
  - `notificacion_nueva` → inserts notification into Room → updates badge count
  - `stock_minimo` → updates insumo stock in Room
  - `pedido_nuevo` / `pedido_atendido` → refreshes pedidos list

### Role-Based Navigation
- Role is stored in `SessionManager` (EncryptedSharedPreferences).
- Bottom navigation adapts to role:
  - `SUBALMACEN` → Pedidos routes to PedidosSubalmacenScreen
  - `ALMACENISTA` / `ADMIN` → Pedidos routes to PedidosAlmacenScreen
  - `ALMACENISTA` / `ADMIN` → Reportes tab visible in bottom nav
- All role checks happen in `BottomNavBar` and `HmngNavHost`.

### Session Security
- JWT token stored in `EncryptedSharedPreferences` (AES-256-GCM).
- `AuthInterceptor` attaches token to every API request.
- `SessionManager.clearSession()` wipes all credentials on logout.

### Paging 3
- `BitacoraScreen` and pedidos screens use Paging 3 for lazy-loading large lists.

### CSV Export
- `ReportesViewModel.exportarCSV()` writes to the public Downloads folder via `MediaStore` (API 29+) or `Environment.DIRECTORY_DOWNLOADS` (API 26-28).
