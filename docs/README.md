# Control Housing Estates

Plataforma web para la administración integral de conjuntos residenciales.

## Documentación

<<<<<<< HEAD:docs/README.md
- [`docs/vision-producto.md`](./docs/vision-producto.md) — Problema, usuarios, propuesta de valor y alcance del MVP.
- [`docs/problema-duro.md`](./docs/problema-duro.md) — Reto técnico, invariante de negocio y evidencia exigida.
- [`docs/historias-usuario.md`](./docs/historias-usuario.md) — Historias de usuario con criterios de aceptación.
- [`docs/uso-ia.md`](./docs/uso-ia.md) — Política de uso de IA y bitácora del equipo.
=======
- [`vision-producto.md`](./vision-producto.md) — Problema, usuarios, propuesta de valor y alcance del MVP.
- [`problema-duro.md`](./problema-duro.md) — Reto técnico, invariante de negocio y evidencia exigida.
- [`historias-usuario.md`](./historias-usuario.md) — Historias de usuario con criterios de aceptación.
- [`uso-ia.md`](./uso-ia.md) — Política de uso de IA y bitácora del equipo.
>>>>>>> 9d3ceecb9e0cf61851084ccda78cbe3e6bb119d9:README.md

## Stack

React (frontend) → Express (backend) → Prisma → PostgreSQL

## Arranque

**Backend** (terminal 1):
```bash
cd backend
npm install
npm run dev
```
Queda en `http://localhost:3000`.

**Frontend** (terminal 2):
```bash
cd frontend
npm install
npm run dev
```
Queda en `http://localhost:5173`, y llama al backend a través del proxy `/api`.

## Prueba (1 comando)

```bash
cd backend
npm test
```

Ejecuta la prueba unitaria de `crearPQRS` y la prueba de integración del flujo trivial de extremo a extremo (`POST /pqrs`).

## Estado del esqueleto

- [x] Estructura del proyecto según el stack (React / Express / Prisma+PostgreSQL).
- [x] Flujo trivial extremo a extremo implementado: "Registrar una PQRS" (frontend → `POST /pqrs` → backend → lista actualizada).
- [x] Primera prueba unitaria escrita (`crearPQRS`) — **pendiente de ejecutarse en el entorno del equipo y confirmar rojo→verde**.
- [ ] Persistencia real con Prisma/PostgreSQL (Semana 5 — actualmente en memoria).
- [ ] CI configurado (Semana 6).
- [ ] Esqueleto fusionado en `main` (acción pendiente del equipo — ver instrucciones abajo).

