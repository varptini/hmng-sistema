# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**HMNG** is a warehouse/inventory management system ("Sistema HMNG Gestión de
Insumos"): supplies, entries/exits, transfers between warehouses (almacén /
subalmacén), employees, roles, dashboard and audit log. Two independent
services: an Express API backend and a React (Vite) frontend.

## Commands

```bash
# Full stack via Docker (postgres + backend + frontend)
docker-compose up

# Backend standalone (inside backend/)
npm run dev          # nodemon src/index.js
npm run db:migrate
npm run db:seed

# Frontend standalone (inside frontend/)
npm run dev          # vite, --host to expose on LAN
npm run build
```

Ports: backend `4000`, frontend `5173`, postgres `5432` (db `hmng_db`, container `hmng_postgres`).

There is no test suite or linter configured.

## Architecture

- `backend/src/index.js` — entrypoint, mounts Express app + Socket.io (`src/socket.js`).
- `backend/src/controllers/` + `src/routes/` — one pair per domain: insumos, entradas, salidas, pedidosAlmacen, pedidosSubalmacen, empleados, usuarios, roles, servicios, dashboard, bitacora, notificaciones.
- `backend/src/middleware/auth.js` — JWT auth; `errorHandler.js` — centralized error handling.
- `backend/src/db/pool.js` — raw `pg` pool (no ORM).
- `backend/src/utils/auditoria.js` — writes to the `bitacora` (audit log) table on mutating actions.
- `frontend/src/pages/` — one page per domain, mirrors backend routes. `src/lib/api.js` wraps fetch calls to the backend; `src/store/` holds client state.

## Environment Variables

Copy `backend/.env.example` to `backend/.env`:
- `DATABASE_URL` — postgres connection string
- `JWT_SECRET` — min 32 chars
- `PORT` — default `4000`
- `FRONTEND_URL` — for CORS

Frontend reads `VITE_API_URL` (set in `docker-compose.yml` for the containerized run).
