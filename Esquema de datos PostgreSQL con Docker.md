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

## Trazabilidad (historia → concepto → tabla → constraint → evidencia)

Cada historia implementada se puede auditar de extremo a extremo: desde el requisito en
`historias-usuario.md` hasta la prueba que lo verifica. Esta cadena es la que responde
"¿de dónde salió esta decisión técnica?" para cada regla de negocio del walking skeleton.

| Historia | Concepto (dominio) | Tabla | Constraint / regla | Evidencia |
|---|---|---|---|---|
| HU-07 — Registrar PQRS | PQRS | `pqrs` | `estado` nace en `PENDIENTE` (`DEFAULT` en `001_schema.sql`) | `backend/tests/pqrs.service.test.js` existe y confirma `estado === "PENDIENTE"`. ⚠️ La sensibilidad RED→GREEN (romper el `DEFAULT`, ver el test fallar, restaurar) todavía no se ha ejecutado ni registrado — el test *existe*, pero eso no es lo mismo que haber *demostrado* que depende del `DEFAULT` y no de otra ruta del código. |
| HU-08 — Gestionar PQRS | PQRS | `pqrs` | Transición de `estado` vía `PATCH /api/pqrs/:id` | ⚠️ sin prueba automatizada todavía — pendiente |
| Listar usuarios (soporte HU-01) | Usuario | `usuarios` | `GET /api/usuarios` sin filtros | ⚠️ verificación manual en `UsuariosPage`, sin test |
| HU-05 / HU-06 — Reservar zona común sin duplicados | Reserva | `reservas` | `UNIQUE(zona_comun_id, fecha_reserva, hora_reserva)` + `version` (bloqueo optimista en la confirmación) | `backend/tests/reservas.concurrencia.test.js` existe (10 solicitudes concurrentes sobre el mismo espacio/horario). ⚠️ El resultado real de una ejecución (p. ej. 1×201 + 9×409) aún no está registrado en el repositorio. |

**Implementado ≠ ejecutado ≠ resultado.** Que un test *exista* en el repositorio solo demuestra
que el comportamiento está *especificado*; no demuestra que se corrió, ni cuál fue el resultado
real. Antes de defender este documento, cada fila con ⚠️ debería poder mostrar las tres cosas
por separado:

| Historia | Implementado | Ejecutado | Resultado observado |
|---|---|---|---|
| HU-07 — sensibilidad RED→GREEN | ✅ test existe | ⬜ pendiente | — |
| HU-08 — test automatizado | ⬜ falta escribir el test | ⬜ pendiente | — |
| Listar usuarios — test automatizado | ⬜ falta escribir el test | ⬜ pendiente | — |
| Concurrencia reservas | ✅ test existe | ⬜ pendiente | — (ej. "1×201 + 9×409") |

*(Tabla para completar por el equipo a medida que se ejecuten las pruebas reales; no se debe
marcar "ejecutado" hasta correr el comando y pegar/registrar la salida real.)*

El caso más fuerte de esta tabla —y el que sustenta el problema duro declarado en
`docs/vision-producto.md` (prevenir asignaciones duplicadas de zonas comunes en el mismo
horario)— es la cadena de reservas, que se puede seguir así:

```
HU-05 / HU-06
    ↓
Reserva
    ↓
reservas
    ↓
UNIQUE(zona_comun_id, fecha_reserva, hora_reserva)
    ↓
test de concurrencia
```

Las dos filas marcadas con ⚠️ son huecos reales de la tabla, no solo advertencias
cosméticas: sin un test que las respalde, esas dos reglas quedan afirmadas en el README
pero no son evidencia reproducible. Antes de dar por cerrada esta trazabilidad conviene
cerrar ambas, aunque sea con un test de integración simple (`PATCH /api/pqrs/:id` cambia
`estado` correctamente; `GET /api/usuarios` devuelve la forma esperada).

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
- `tests/pqrs.service.test.js` — la primera prueba TDD del proyecto (verifica
  `estado === "PENDIENTE"`). ⚠️ Existe el test; la evidencia histórica de su ciclo
  RED → GREEN (romper la regla, ver fallar, restaurar, ver pasar) todavía no se ha
  ejecutado ni queda registrada en el repositorio.
- `tests/reservas.concurrencia.test.js` — corre el escenario exigido en
  `docs/problema-duro.md` (10 solicitudes concurrentes para el mismo espacio/horario).
  ⚠️ El test está implementado; el resultado real de correrlo (cuántas respondieron
  `201` y cuántas `409`, en qué condiciones) aún no está documentado.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev               # http://localhost:5173
```

## Notas de diseño (diferencias entre modelo-dominio.md y modelo-DB.md)

Al traducir ambos modelos a SQL se encontraron un par de puntos donde no coincidían
exactamente. Cada diferencia se documenta con la cadena completa: qué se encontró, qué
decidió el equipo, por qué, en qué artefacto quedó, y si esa decisión ya está verificada.

- **`reservas` — falta `hora_reserva`.**
  - *Diferencia:* el modelo de dominio exige `UNIQUE(zona_comun_id, fecha_reserva, hora_reserva)`,
    pero `modelo-DB.md` no incluía la columna `hora_reserva`.
  - *Decisión:* agregar la columna `hora_reserva` a la tabla `reservas`.
  - *Razón:* sin ella, el `UNIQUE` no puede distinguir dos reservas del mismo día en
    horarios distintos — y esa distinción es justo lo que exige la regla de disponibilidad
    de HU-06.
  - *Artefacto actualizado:* `database/init/001_schema.sql` (columna `hora_reserva`) y
    `backend/prisma/schema.prisma`.
  - *Verificación:* ⚠️ pendiente — falta una prueba explícita que confirme que dos
    reservas en la misma `fecha_reserva` pero distinta `hora_reserva` sí se permiten, y que
    la misma fecha+hora sí choca contra el `UNIQUE`.

- **`visitantes`, `correspondencias`, `pqrs` — relación de doble actor.**
  - *Diferencia:* el modelo de dominio define relaciones dobles (un residente *radica* una
    PQRS y un administrador la *gestiona*; un residente *registra* un visitante y portería
    lo *verifica*), pero el modelo relacional solo persiste una única FK
    (`usuarios_cedula` / `residente_cedula`).
  - *Decisión:* respetar el modelo relacional tal como se entregó y no agregar la segunda FK
    por ahora.
  - *Razón:* ninguna historia priorizada del walking skeleton actual depende de registrar
    el segundo actor (quién gestionó/verificó); agregarlo ahora sería ampliar alcance sin
    una HU que lo exija.
  - *Artefacto actualizado:* ninguno — es una decisión de no-cambio, documentada aquí para
    que quede explícita y no se confunda con un olvido.
  - *Verificación:* n/a mientras no se implemente. Si se retoma, faltaría agregar
    `gestionado_por_cedula` en `pqrs` / `verificado_por_cedula` en `visitantes`, su
    migración de Prisma, y un test que la ejercite.

- **`pqrs.respuesta` — `NOT NULL` vs opcional.**
  - *Diferencia:* `modelo-DB.md` la define `NOT NULL`, pero una PQRS no tiene respuesta al
    momento de radicarse.
  - *Decisión:* dejar `respuesta` como columna opcional (`NULL` por defecto).
  - *Razón:* consistente con HU-07 — la PQRS "nace" en `PENDIENTE`, sin respuesta todavía.
  - *Artefacto actualizado:* `database/init/001_schema.sql`.
  - *Verificación:* cubierta indirectamente por `pqrs.service.test.js` (crea una PQRS sin
    `respuesta` y no falla). ⚠️ No hay una aserción explícita que verifique que `respuesta`
    admite `NULL` — sería una línea de prueba económica de agregar.

- **Valores de los enums de estado.**
  - *Diferencia:* `modelo-DB.md` no especificaba los valores de `estado_paquete_enum` ni
    `estado_pqrs_enum`.
  - *Decisión:* `estado_paquete_enum` = `RECIBIDO → NOTIFICADO → ENTREGADO` (tomado
    directamente de HU-04); `estado_pqrs_enum` inicia en `PENDIENTE` (HU-07), con
    `EN_PROCESO`/`RESUELTO` inferidos de HU-08; `estado_reserva_enum` renombró
    `CANCELADA` → `RECHAZADA` para calzar con el lenguaje de HU-06.
  - *Razón:* `RECIBIDO/NOTIFICADO/ENTREGADO` y `PENDIENTE` están tomados literalmente de las
    historias; `EN_PROCESO`/`RESUELTO`/`RECHAZADA` son una interpretación del equipo, no una
    cita textual.
  - *Artefacto actualizado:* `database/init/001_schema.sql`.
  - *Verificación:* ⚠️ los tres valores inferidos (`EN_PROCESO`, `RESUELTO`, `RECHAZADA`) no
    han sido confirmados por el equipo/tutor como los nombres correctos — queda abierto y
    debería cerrarse antes de defender el documento como definitivo.

## Próximos pasos sugeridos

- Completar los endpoints restantes (visitantes, correspondencias, PQRS) siguiendo el
  mismo patrón de capas (`routes` → `controllers` → `services` → Prisma).
- Agregar autenticación real (actualmente `credenciales` es solo un campo de texto).
- Añadir pruebas automatizadas sobre el walking skeleton antes de seguir creciendo.
