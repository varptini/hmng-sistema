# HMNG — Sistema de Gestión de Insumos

**Hospital de la Madre y el Niño Guerrerense**  
Stack: Node.js · Express · PostgreSQL 16 · React 18 · Shadcn/ui · Socket.io · Docker

---

## Requisitos

- Docker + Docker Compose
- Puertos libres: 4000, 5173, 5432

---

## Arranque rápido

```bash
cd hmng-sistema
docker compose up --build
```

| Servicio    | URL                        |
|-------------|----------------------------|
| Frontend    | http://localhost:5173       |
| Backend API | http://localhost:4000/api   |
| Health      | http://localhost:4000/health|

**Credenciales iniciales:**
```
Usuario: admin
Contraseña: Admin2025!
```

---

## Estructura del proyecto

```
hmng-sistema/
├── docker-compose.yml
├── docker/
│   └── init.sql          ← Schema + datos semilla
├── backend/
│   ├── src/
│   │   ├── controllers/  ← Lógica de negocio
│   │   ├── routes/       ← Definición de endpoints
│   │   ├── middleware/    ← Auth JWT, errores
│   │   ├── db/pool.js    ← Conexión PostgreSQL
│   │   ├── socket.js     ← Notificaciones tiempo real
│   │   └── utils/        ← Logger, auditoría
│   └── package.json
└── frontend/
    └── src/
        ├── pages/        ← Dashboard, Insumos, Pedidos...
        ├── components/   ← Layout, UI
        ├── store/        ← Zustand (auth)
        └── lib/          ← Axios, utilidades
```

---

## Módulos implementados

| Módulo | Rol requerido |
|--------|--------------|
| Dashboard con estadísticas | Todos |
| Catálogo de Insumos (CRUD) | Admin / Abastecedor |
| Entradas al sub-almacén | Admin / Abastecedor |
| Pedidos al sub-almacén | Todos |
| Pedidos al almacén general | Admin / Abastecedor |
| Bitácora de movimientos | Todos |
| Servicios hospitalarios | Admin |
| Empleados | Admin |
| Usuarios y roles | Admin |
| Notificaciones tiempo real | Todos |

---

## API Endpoints principales

```
POST   /api/auth/login
GET    /api/auth/profile

GET    /api/insumos?search=&alerta=stock|caducidad|caducado
POST   /api/insumos
PUT    /api/insumos/:id
DELETE /api/insumos/:id
GET    /api/insumos/alertas

POST   /api/entradas
GET    /api/entradas

GET    /api/pedidos-subalmacen
POST   /api/pedidos-subalmacen
PUT    /api/pedidos-subalmacen/:id/atender
PUT    /api/pedidos-subalmacen/:id/cancelar

GET    /api/dashboard
GET    /api/bitacora
GET    /api/notificaciones
GET    /api/reportes/inventario
GET    /api/reportes/movimientos
```

---

## Roles del sistema

| Rol | Permisos |
|-----|---------|
| Administrador | Acceso total |
| Abastecedor | Insumos, entradas, pedidos al almacén |
| Suministrador | Atender pedidos del sub-almacén |
| Responsable de Servicio | Crear pedidos al sub-almacén |

---

## Notas de seguridad

- Contraseñas con **bcrypt** (rounds=12)
- Autenticación **JWT** (8h de duración)
- **Rate limiting**: 200 req/15min
- Headers de seguridad con **Helmet**
- Consultas con **parámetros preparados** (sin SQL injection)
- Auditoría automática de acciones críticas
