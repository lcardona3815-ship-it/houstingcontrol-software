#*"Control Housing Estates no es solo un prototipo conceptual; es la columna vertebral técnica de una solución real para la gestión residencial.Al combinar la portabilidad de Docker con la robustez de PostgreSQL, garantizamos un entorno de datos aislado, reproducible y consistente. A través de un Walking Skeleton de arquitectura limpia (React, Express, Prisma y PostgreSQL), hemos conectado de extremo a extremo el flujo real de valor: desde la interacción del usuario en la interfaz hasta la persistencia y reglas de negocio en la base de datos.  Más allá de cubrir operaciones CRUD tradicionales, esta infraestructura está diseñada para soportar desafíos técnicos de alta exigencia, tales como la prevención de condiciones de carrera y reservas duplicadas en tiempo real mediante restricciones de unicidad compuesta y pruebas automatizadas de concurrencia masiva. Es la integración perfecta entre diseño de dominio, rigor de ingeniería de software y valor entregado a las historias de usuario."*

# Trazabilidad — historia → concepto → tabla → constraint → evidencia

Este documento responde al punto más señalado en la revisión de Corte 1: que la
cadena `historia → modelo de dominio → modelo de datos → PostgreSQL → API →
frontend` **exista en el repositorio y se pueda recorrer con archivos y
ejecuciones reales**, no solo describirse en el README.

## A. Justificación de cada tabla

Para cada tabla: *"Esta tabla existe porque en ___ aparece ___."*

| Tabla | Aparece en | Justificación |
|---|---|---|
| `roles` | `modelo-dominio.md`, enum `Rol` | Catálogo de los 5 roles (`ADMINISTRADOR`, `RESIDENTE`, `PORTERIA`, `CONSEJO`, `MANTENIMIENTO`) que HU-01 usa para restringir acceso por rol. |
| `usuarios` | `modelo-dominio.md`, clase `Usuario`; HU-01 | Todo actor autenticado del sistema (residente, portero, administrador). HU-01 exige que las credenciales identifiquen un rol. |
| `zonas_comunes` | `modelo-dominio.md`, clase `ZonaComun`; HU-05 | HU-05 exige "consultar las zonas disponibles" antes de reservar; sin esta tabla no hay qué listar ni a qué `zona_comun_id` referenciar desde `reservas`. |
| `visitantes` | `modelo-dominio.md`, clase `Visitante`; HU-02, HU-03 | HU-02 exige que el residente registre nombre, documento y fecha prevista; HU-03 exige que portería lo pueda consultar y registrar el ingreso. Sin esta tabla no hay dónde persistir esos campos. |
| `correspondencias` | `modelo-dominio.md`, clase `Correspondencia`; HU-04 | HU-04 exige registrar destinatario, descripción, fecha de recepción y un estado con ciclo de vida (`RECIBIDO → NOTIFICADO → ENTREGADO`). |
| `pqrs` | `modelo-dominio.md`, clase `PQRS`; HU-07, HU-08 | HU-07 exige generar un identificador y un estado inicial `PENDIENTE`; HU-08 exige que el administrador filtre por estado y registre una respuesta. |
| `reservas` | `modelo-dominio.md`, clase `Reserva` (con la nota `UNIQUE`); HU-05, HU-06 | HU-05 exige crear la reserva si el horario está disponible; HU-06 (el problema duro) exige que, ante concurrencia, exactamente una prospere. |

Ninguna tabla se agregó por conveniencia de implementación sin un origen en el
modelo de dominio o en una historia — si el equipo llega a agregar una nueva,
debe poder completar esta misma fila antes de escribir el `CREATE TABLE`.

## B. HU-07 — cadena completa (flujo de referencia del walking skeleton)

```
HU-07 (historias-usuario.md)
  ↓ criterio de aceptación: "Inicialmente queda en estado PENDIENTE"
  ↓ servicio: backend/src/services/pqrs.service.js → crearPQRS()
  ↓ endpoint: POST /api/pqrs (backend/src/routes/pqrs.routes.js)
  ↓ persistencia: tabla pqrs, columna estado DEFAULT 'PENDIENTE'
     (database/init/001_schema.sql)
  ↓ test: backend/tests/pqrs.service.test.js
     → crea una PQRS real contra la base de datos y afirma
       pqrs.estado === "PENDIENTE"
```

**Cómo comprobarlo sin mirar el código:** correr `npm run test -- pqrs.service`
con la base de datos levantada y ver el resultado. El test falla si alguien
cambia el `DEFAULT` de la columna o si el servicio empieza a pasar un `estado`
distinto explícitamente.

**Prueba de sensibilidad (RED) pendiente de dejar registrada por el equipo:**
la revisión exige comprobar que el test se pone en rojo si se rompe la regla,
no solo que está en verde. Procedimiento a documentar (con captura o log real,
no solo la afirmación):
1. Cambiar temporalmente `DEFAULT 'PENDIENTE'` por `DEFAULT 'EN_PROCESO'` en
   `database/init/001_schema.sql`, o forzar `estado: "EN_PROCESO"` dentro de
   `crearPQRS`.
2. Re-aplicar la migración / reiniciar la base.
3. Correr `npm run test -- pqrs.service` y confirmar que falla (RED).
4. Revertir el cambio y confirmar que vuelve a pasar (GREEN).
5. Guardar la salida de la terminal de ambos pasos como evidencia (por
   ejemplo en `docs/evidencia/`).

Esto no se automatizó en este repo porque romper el esquema a propósito no
debe quedar commiteado; es un procedimiento que el equipo ejecuta una vez y
documenta con evidencia real, no una afirmación en el README.

## C. Problema duro / HU-06 — cadena completa

```
Problema duro (docs/problema-duro.md)
  ↓ HU-06 (historias-usuario.md)
  ↓ regla de concurrencia: "en ningún momento pueden coexistir dos reservas
    para la misma zona_comun_id + fecha_reserva + hora_reserva"
  ↓ mecanismo: UNIQUE (zona_comun_id, fecha_reserva, hora_reserva)
    (database/init/001_schema.sql, tabla reservas)
  ↓ test concurrente: backend/tests/reservas.concurrencia.test.js
     → 10 solicitudes simultáneas (Promise.all) contra POST /api/reservas
       para el mismo (zona, fecha, hora)
  ↓ resultado observable: exactamente 1 respuesta 201, 9 respuestas 409,
    y 1 fila en la tabla reservas para ese slot — capturado en un archivo
    de evidencia JSON (ver sección D)
```

### Preguntas de defensa (para responder sin mirar el código)

**¿Qué condición concreta protege el `UNIQUE`?**
Que no exista más de una fila con la misma combinación
`(zona_comun_id, fecha_reserva, hora_reserva)`. Postgres la aplica sobre el
índice único en el momento del `INSERT`, dentro de la misma transacción — por
eso protege la *creación* concurrente de reservas nuevas.

**¿Qué condición concreta protege `version`?**
Que un `UPDATE` sobre una reserva **que ya existe** solo se aplique si nadie
más la modificó desde que se leyó. Protege una *transición de estado*
concurrente (por ejemplo, confirmar y cancelar la misma reserva al mismo
tiempo), no la creación de filas nuevas.

**¿Por qué el bloqueo optimista no sustituye al `UNIQUE` para el otro
problema?**
Porque el patrón de `version` (`leer → comparar → UPDATE WHERE id=? AND
version=?`) solo tiene sentido sobre una fila que **ya existe**. Diez
solicitudes de reserva concurrentes son diez intentos de creación — no hay
una fila común que leer y comparar antes del `INSERT`, así que no hay
`version` que consultar. Si se intentara resolver la duplicación con un
patrón "leer disponibilidad → si está libre, insertar" en la capa de
aplicación, las diez solicitudes podrían leer "disponible" al mismo tiempo,
antes de que cualquiera hubiera insertado, y las diez terminarían insertando.
El detalle completo está en `docs/decisiones-tecnicas.md`.

## D. Qué debe registrar la evidencia de concurrencia (y por qué antes no alcanzaba)

La revisión señala, con razón, que **"exactamente una confirmada" no es
suficiente si solo queda escrito en el README**. La evidencia debe dejar
registrado, por ejecución real:

- Qué operación se ejecutó (`POST /api/reservas`).
- Qué recurso se compartió (`zona_comun_id` usado).
- Qué fecha/hora se usó (`fecha_reserva`, `hora_reserva`).
- Cuántas solicitudes se lanzaron (10).
- Bajo qué estado inicial (tabla `reservas` vacía para ese slot antes de
  correr el escenario).
- Qué resultado produjo **cada** solicitud (código de estado HTTP por
  residente).
- Qué condición se consideró éxito (exactamente 1 `201`, el resto `409`, y 1
  fila en base de datos).
- Cuántas veces se ejecutó el escenario (el test ahora corre el escenario 3
  veces, con un slot distinto cada vez, y falla si cualquiera de las 3
  corridas no cumple la condición de éxito).

`backend/tests/reservas.concurrencia.test.js` ahora escribe esa información
en `backend/tests/evidencia/concurrencia-<timestamp>.json` cada vez que se
ejecuta, en lugar de solo afirmar el resultado en prosa. Ese archivo generado
es la evidencia reproducible que pide la revisión — no reemplaza correrlo en
el entorno real del equipo, pero deja el mecanismo listo para que cualquiera
lo ejecute y obtenga la evidencia con datos reales, no inventados.
