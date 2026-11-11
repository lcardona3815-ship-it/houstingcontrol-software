# Decisiones técnicas — Housing Control

Registro de las decisiones técnicas del proyecto: qué se eligió, qué alternativas se consideraron, por qué se descartaron y qué limitaciones quedan. Las limitaciones están escritas a propósito: una decisión sin sus límites no es una decisión documentada.


---

## DT-01 — Evitar reservas duplicadas (problema duro)

### Contexto

Invariante del negocio: en ningún momento pueden coexistir dos reservas para la misma zona común en el mismo día y hora. Si dos residentes la piden al mismo tiempo, una prospera y la otra se rechaza.

### El riesgo: verificar y luego guardar no alcanza

`ReservaService.crearReserva` primero pregunta si el horario está libre (`existsByZonasComunesIdAndFechaReserva`) y después guarda. Entre la pregunta y el guardado hay un intervalo: dos hilos pueden preguntar a la vez, los dos reciben "libre", y los dos guardan. Es una condición de carrera clásica (*check-then-act*). La verificación previa sola no garantiza la regla.

### Alternativas evaluadas

| Alternativa | Qué garantiza | Por qué se eligió o se descartó |
|---|---|---|
| Solo verificar antes de guardar (`existsBy...`) | Nada bajo concurrencia | **Descartada como única defensa**: es la condición de carrera descrita arriba. Se conserva como barrera previa. |
| `@Version` (bloqueo optimista) | Detecta que alguien modificó **una fila que ya existía** antes de que tú la guardaras | **Descartada para este caso**: dos reservas nuevas son dos `INSERT` y no hay fila previa que versionar, así que ninguna de las dos choca con la otra. Sí serviría para modificar una reserva existente (reprogramar o cancelar), algo que hoy no existe. |
| `UNIQUE (zonas_comunes_id, fecha_reserva)` en la base de datos | La base rechaza el segundo `INSERT` sin importar en qué orden se ejecuten las peticiones | **Elegida.** La garantía vive en la base de datos, que es el único punto por el que pasan todas las peticiones. |

### Decisión

Dos capas, cada una con su función:

1. **Barrera previa (`existsBy...`)**: rechaza rápido el caso común (horario ya ocupado) con un mensaje claro, sin ir a escribir a la base.
2. **Restricción `UNIQUE` (`unique_reserva_zona_fecha`)**: es la garantía real. Si dos peticiones pasan la barrera a la vez, la base rechaza a la segunda.

La restricción está declarada en dos lugares que deben mantenerse alineados:
- `database/v1-initial-schema.sql` (PostgreSQL).
- `@UniqueConstraint` en la entidad `Reserva` (es lo que crea la tabla en H2 para las pruebas).

Cómo responde la API en cada caso (`ReservaController`):

| Situación | Excepción | Respuesta |
|---|---|---|
| El horario ya figuraba ocupado al consultar | `IllegalStateException` | `409` "La zona común ya se encuentra reservada…" |
| Pasaron la barrera a la vez y la base rechazó la segunda | `DataIntegrityViolationException` | `409` "Colisión detectada en BD" |
| Datos inválidos (nulos o fecha pasada) | `IllegalArgumentException` | `400` |

### Evidencia

Prueba: [`ReservaConcurrenciaIntegrationTest`](../backend/software/src/test/java/com/housingcontrol/software/infrastructure/controller/ReservaConcurrenciaIntegrationTest.java).

- Un pool de 10 hilos y un `CountDownLatch` que dispara las 10 peticiones a la vez.
- Diez residentes distintos, misma zona, mismo horario.
- Pasa solo si exactamente una petición responde `201`, nueve responden `409`, y en la base queda una sola reserva `CONFIRMADA` para ese horario.
- Se repite 5 veces (`@RepeatedTest`), porque una condición de carrera no siempre aparece en un solo intento.

**Resultado de la ejecución (completar con la salida real):**

```
Comando: ./mvnw test -Dtest=ReservaConcurrenciaIntegrationTest
Fecha:
Resultado:
```

**Prueba de que la prueba detecta el defecto (opcional, completar si se ejecuta):** quitar temporalmente `@UniqueConstraint` y la verificación `existsBy...`, ejecutar la prueba y confirmar que falla (más de un `201`); después restaurar ambas cosas. Resultado observado:

### Limitaciones

- **La prueba corre sobre H2, no sobre PostgreSQL.** El comportamiento de los índices únicos ante transacciones concurrentes puede diferir entre motores. Pendiente: repetir la prueba con Testcontainers y PostgreSQL (ver [`deuda-tecnica.md`](./deuda-tecnica.md)).
- **No se midió rendimiento.** El problema duro planteaba comparar el rendimiento de ambos mecanismos. La comparación de este documento es conceptual (qué garantiza cada uno), no un benchmark.
- **La unicidad es sobre el instante exacto.** Hay un solo campo `fecha_reserva` y no existe hora de fin. Dos reservas con horas distintas pero solapadas (por ejemplo 18:00–20:00 y 19:00–21:00) no se detectan como conflicto. Además, `problema-duro.md` hablaba de una restricción sobre zona, fecha y hora; lo implementado es zona más `fecha_reserva`.
- **Los rechazados no se reprograman.** El invariante admite "rechazadas o reprogramadas"; hoy solo se rechaza con `409`. El estado `REPROGRAMADA` existe en el enum de la base, pero ninguna regla lo usa.
- **La prueba omite el filtro de seguridad.** Usa `MockMvc` sin Spring Security y fija la identidad a mano en cada hilo. Valida el flujo controlador → servicio → JPA → base, no la autenticación.

---

## DT-02 — Arquitectura en capas

| | |
|---|---|
| Decisión | `domain`, `application` (DTO y servicios) e `infrastructure` (controladores y repositorios). |
| Por qué | Separa las reglas de negocio de HTTP y de la persistencia, y permite probar los servicios con Mockito sin levantar Spring. |
| Alternativa descartada | Lógica en los controladores: más rápida de escribir, pero difícil de probar y mantener. |
| Limitación | `PqrsController` de la primera versión hablaba directo con el repositorio; las historias posteriores usan servicio. El estilo no es uniforme en todo el código. |

## DT-03 — Perfil `h2` además de PostgreSQL

| | |
|---|---|
| Decisión | Un perfil `h2` que arranca la API con base en memoria y datos de demostración, además de la configuración con PostgreSQL. |
| Por qué | Parte del equipo no tenía la virtualización habilitada para ejecutar Docker. El perfil permite demostrar y probar la API sin él. |
| Alternativa descartada | Solo PostgreSQL: bloquea la ejecución en equipos sin Docker. |
| Limitación | H2 no valida el script SQL real ni los tipos enumerados de PostgreSQL. |

## DT-04 — Un único proyecto vigente en `backend/software`

| | |
|---|---|
| Decisión | Unificar el código en una sola ruta. |
| Por qué | Evita mantener dos copias activas y la confusión sobre cuál es la correcta. |
| Pendiente | La carpeta `backend/walkin-skeleton` sigue en el repositorio; conviene eliminarla o marcarla como histórica. |

## DT-05 — Manejo de errores con `try/catch` por endpoint

| | |
|---|---|
| Decisión actual | Cada controlador traduce las excepciones a códigos HTTP con `try/catch`. |
| Por qué | Es lo más simple para empezar y es explícito. |
| Limitación | Se repite en cada ruta, y un mismo tipo de excepción (`IllegalArgumentException`) significa cosas distintas según el método. |
| Mejora prevista | Excepciones propias y un `@ControllerAdvice` central (ver [`deuda-tecnica.md`](./deuda-tecnica.md)). |

---

## Decisiones pendientes

- **Autenticación y autorización.** El código usa `@PreAuthorize` y `SecurityContextHolder`, pero el repositorio no contiene una configuración de seguridad (`SecurityFilterChain`) ni la emisión o validación de tokens. Hay que decidir el mecanismo (por ejemplo JWT o sesión) e implementarlo. Contrastar con [`remediación.md`](./remediaci%C3%B3n.md), que describe como implementado lo que hoy solo aparece en las pruebas.
- **Nombres de los roles.** Los roles de la base de datos (por ejemplo "Residente Propietario") no coinciden con los que esperan las anotaciones (`RESIDENTE`, `PORTERIA`, `ADMIN`).
