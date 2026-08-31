# Control Housing Estates — Esquema de datos + Walking Skeleton

Este repositorio contiene:

1. **Esquema de datos PostgreSQL con Docker**, derivado de `modelo-dominio.md` y `modelo-DB.md`.
2. **Walking skeleton** (React + Express + Prisma + PostgreSQL) que conecta de punta a
   punta interfaz → API → base de datos con historias de usuario reales del proyecto.
3. **La evidencia automatizada del problema duro** (concurrencia en reservas, HU-06) y
   el documento de decisión técnica correspondiente.

Ver `docs/vision-producto.md`, `docs/problema-duro.md` y `docs/historias-usuario.md`
para el contexto completo del producto.

## Estructura

```
sistema-residencial/
├── docker-compose.yml          # Levanta PostgreSQL + Adminer
├── database/init/              # Scripts SQL (fuente de verdad del esquema)
│   ├── 001_schema.sql
│   └── 002_seed.sql
├── docs/
│   ├── vision-producto.md
│   ├── problema-duro.md
│   ├── historias-usuario.md
│   └── decisiones-tecnicas.md  # UNIQUE constraint vs bloqueo optimista
├── backend/                    # API Express + Prisma
│   ├── prisma/schema.prisma
│   ├── prisma/migrations/
│   ├── src/
│   └── tests/                  # TDD (crearPQRS) + prueba de concurrencia (HU-06)
└── frontend/                   # React (Vite)
    └── src/
```

## ¿Por qué "walking skeleton"?

Un walking skeleton es la implementación mínima y real de una arquitectura que conecta
todas las capas (UI, API, base de datos) para una porción muy delgada de funcionalidad.
No es un prototipo desechable: es la base sobre la que se construyen el resto de las
historias de usuario. `historias-usuario.md` define el flujo trivial de referencia como
*"Registrar una PQRS"* (Frontend → Backend → Base de datos → Backend → Frontend), con su
primera prueba TDD (`crearPQRS()` → `estado === "PENDIENTE"`). A partir de ahí se
implementaron estas historias de extremo a extremo:

1. **HU-07 — Registrar PQRS** (flujo de referencia). `POST /api/pqrs` →
   `pqrs.service.crearPQRS` → Prisma → PostgreSQL. La regla "nace en `PENDIENTE`" vive en
   el `DEFAULT` de la base de datos y está cubierta por el test TDD en
   `backend/tests/pqrs.service.test.js`.
2. **HU-08 — Gestionar PQRS.** `GET /api/pqrs`, `PATCH /api/pqrs/:id`.
3. **Listar usuarios** (soporte para HU-01/administración). `GET /api/usuarios` → tabla
   renderizada en React (`UsuariosPage`).
4. **HU-05 / HU-06 — Reservar zona común sin duplicados** (el problema duro).
   `POST /api/reservas` respeta el `UNIQUE(zona_comun_id, fecha_reserva, hora_reserva)`;
   `PATCH /api/reservas/:id/confirmar` usa `version` como bloqueo optimista para
   transiciones de estado concurrentes sobre una reserva ya existente. La decisión de
   por qué el UNIQUE constraint —y no el bloqueo optimista— es el mecanismo que evita la
   reserva duplicada está documentada en `docs/decisiones-tecnicas.md`.

## Cómo levantarlo

### 1. Base de datos

```bash
cp .env.example .env
docker compose up -d
```

Esto crea el esquema (`001_schema.sql`) y los datos semilla (`002_seed.sql`) automáticamente
la primera vez que se crea el volumen. Adminer queda disponible en `http://localhost:8080`.

### 2. Backend

```bash
cd backend
cp .env.example .env
npm install
npm run prisma:generate
npm run prisma:migrate   # aplica backend/prisma/migrations
npm run dev              # http://localhost:4000
```

### Pruebas

Requieren la base de datos levantada (`docker compose up -d`) y las migraciones
aplicadas.

```bash
cd backend
npm run test
```

Incluye:
- `tests/pqrs.service.test.js` — la primera prueba TDD del proyecto (RED → GREEN).
- `tests/reservas.concurrencia.test.js` — la evidencia exigida en `docs/problema-duro.md`:
  10 solicitudes concurrentes para el mismo espacio/horario, exactamente una confirmada.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev               # http://localhost:5173
```

## Notas de diseño (diferencias entre modelo-dominio.md y modelo-DB.md)

Al traducir ambos modelos a SQL se encontraron un par de puntos donde no coincidían
exactamente; se documentan aquí para que el equipo decida si ampliarlos:

- **`reservas`**: la nota del modelo de dominio dice
  `UNIQUE(zona_comun_id, fecha_reserva, hora_reserva)`, pero `modelo-DB.md` no incluye la
  columna `hora_reserva`. Se agregó la columna para poder honrar esa restricción.
- **`visitantes`, `correspondencias`, `pqrs`**: el modelo de dominio define relaciones
  dobles (por ejemplo, un residente *radica* una PQRS y un administrador la *gestiona*;
  un residente *registra* un visitante y portería lo *verifica*), pero el modelo
  relacional solo persiste una única FK (`usuarios_cedula` / `residente_cedula`). Se
  respetó el modelo relacional tal como se entregó. Si se necesita conservar ambos
  actores, habría que agregar una segunda columna FK (por ejemplo `gestionado_por_cedula`
  en `pqrs`, `verificado_por_cedula` en `visitantes`).
- **`pqrs.respuesta`**: en `modelo-DB.md` aparece como `NOT NULL`, pero una PQRS no tiene
  respuesta al momento de radicarse; se dejó como columna opcional (`NULL` por defecto).
- **Valores de los enums de estado**: `modelo-DB.md` no especificaba los valores de
  `estado_paquete_enum` ni `estado_pqrs_enum`. Se ajustaron a lo que sí define
  `historias-usuario.md`: `estado_paquete_enum` = `RECIBIDO → NOTIFICADO → ENTREGADO`
  (HU-04) y `estado_pqrs_enum` inicia en `PENDIENTE` (HU-07); `EN_PROCESO`/`RESUELTO` se
  infirieron de HU-08 ("puede cambiar el estado") y quedan abiertos a ajuste del equipo.
  `estado_reserva_enum` se renombró `CANCELADA` → `RECHAZADA` para calzar con el lenguaje
  de HU-06 ("deben ser rechazadas").

## Próximos pasos sugeridos

- Completar los endpoints restantes (visitantes, correspondencias, PQRS) siguiendo el
  mismo patrón de capas (`routes` → `controllers` → `services` → Prisma).
- Agregar autenticación real (actualmente `credenciales` es solo un campo de texto).
- Añadir pruebas automatizadas sobre el walking skeleton antes de seguir creciendo.

