# Control Housing Estates

Plataforma web para la administración integral de conjuntos residenciales.

## Documentación

- [`docs/vision-producto.md`](./docs/vision-producto.md) — Problema, usuarios, propuesta de valor y alcance del MVP.
- [`problema-duro.md`](./problema-duro.md) — Reto técnico, invariante de negocio y evidencia exigida.
- [`historias-usuario.md`](./historias-usuario.md) — Historias de usuario con criterios de aceptación.
- [`docs/uso-ia.md`](./docs/uso-ia.md) — Política de uso de IA y bitácora del equipo.

## Stack

React (frontend) → Express (backend) → Prisma → PostgreSQL

## Arranque (2 comandos)

```bash
cd backend
npm install
npm run dev
```

Levanta el backend en `http://localhost:3000`.

## Prueba (1 comando)

```bash
cd backend
npm test
```

Ejecuta la prueba unitaria de `crearPQRS` y la prueba de integración del flujo trivial de extremo a extremo (`POST /pqrs`), que cubre la primera historia demostrable del proyecto.

## Estado del esqueleto

- [x] Flujo trivial extremo a extremo implementado: "Registrar una PQRS" (`POST /pqrs`).
- [x] Primera prueba unitaria escrita y pasando (`crearPQRS`).
- [ ] Persistencia real con Prisma/PostgreSQL (Semana 5 — actualmente en memoria).
- [ ] CI configurado (Semana 6).
