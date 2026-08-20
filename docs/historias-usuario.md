# Historias de usuario — Control Housing Estates

## Usuarios, necesidades y restricciones

| Usuario | Necesidad |
|---|---|
| Administrador | Gestionar usuarios, PQRS, reservas y reportes. |
| Residente | Reservar zonas, registrar PQRS y consultar información. |
| Portería | Registrar visitantes y correspondencia. |

## Estructura de las historias

```
Como [tipo de usuario],
quiero [acción],
para [beneficio].
```

---

### HU-01 — Registro / inicio de sesión

**Como** residente, **quiero** iniciar sesión en la plataforma **para** acceder a las funcionalidades correspondientes a mi rol.

**Criterios de aceptación:**
- Dado un usuario registrado, cuando introduce credenciales válidas, debe ingresar al sistema.
- Si las credenciales son incorrectas, debe mostrar un mensaje de error.
- El usuario debe acceder solamente a las funcionalidades correspondientes a su rol.

---

### HU-02 — Registro de visitantes (residente)

**Como** residente, **quiero** registrar previamente un visitante **para** facilitar su ingreso al conjunto.

**Criterios de aceptación:**
- El residente puede registrar nombre del visitante.
- Puede registrar documento o identificación.
- Puede indicar fecha prevista de visita.
- Portería puede consultar el visitante registrado.
- Un visitante registrado debe quedar asociado al residente.

---

### HU-03 — Consulta de visitantes en portería

**Como** portero, **quiero** consultar los visitantes registrados **para** verificar si una persona está autorizada para ingresar.

**Criterios de aceptación:**
- Portería puede buscar al visitante.
- El sistema muestra el residente asociado.
- El portero puede registrar el ingreso.
- El sistema almacena fecha y hora del ingreso.

---

### HU-04 — Registrar correspondencia

**Como** portero, **quiero** registrar un paquete recibido **para** informar al residente y mantener trazabilidad.

**Criterios de aceptación:**
- Se registra destinatario.
- Se registra descripción del paquete.
- Se registra fecha de recepción.
- El paquete queda asociado a un estado: `RECIBIDO → NOTIFICADO → ENTREGADO`.

---

### HU-05 — Reservar zona común

> Una de las historias más importantes del MVP.

**Como** residente, **quiero** reservar una zona común **para** utilizarla en una fecha y horario disponibles.

**Criterios de aceptación:**
- El residente puede consultar las zonas disponibles.
- Puede seleccionar fecha y horario.
- El sistema verifica disponibilidad.
- Si está disponible, se crea la reserva.
- Si ya existe una reserva para ese horario, se rechaza.

---

### HU-06 — Evitar reservas duplicadas

> Historia directamente relacionada con el [problema duro](./problema-duro.md).

**Como** residente, **quiero** que el sistema impida reservar una zona común que ya fue reservada para el mismo horario **para** evitar conflictos.

**Criterios de aceptación:**
- Dos solicitudes simultáneas para la misma zona no pueden generar dos reservas confirmadas.
- Exactamente una solicitud debe quedar confirmada.
- Las demás deben ser rechazadas o enviadas a reprogramación.
- La base de datos debe mantener un estado consistente.

---

### HU-07 — Registrar PQRS

**Como** residente, **quiero** registrar una PQRS **para** comunicar una solicitud, petición, queja o reclamo a la administración.

**Criterios de aceptación:**
- El residente puede registrar asunto.
- Puede escribir una descripción.
- El sistema genera un identificador.
- La PQRS queda asociada al residente.
- Inicialmente queda en estado `PENDIENTE`.

---

### HU-08 — Gestionar PQRS

**Como** administrador, **quiero** consultar y actualizar las PQRS **para** hacer seguimiento a las solicitudes de los residentes.

**Criterios de aceptación:**
- El administrador puede visualizar las PQRS.
- Puede filtrar por estado.
- Puede cambiar el estado.
- Puede registrar una respuesta.
- El residente puede consultar el estado de su solicitud.

---

## Revisión cruzada

**Equipo evaluador:** Equipo X

| Aspecto | Resultado |
|---|---|
| Historias comprensibles | ✅ |
| Usuarios claramente identificados | ✅ |
| Criterios verificables | ⚠️ |
| Historias demasiado grandes | ⚠️ |
| Funcionalidades fuera del MVP | ❌ |

## Requisitos no funcionales

Un requisito **funcional** dice: *"El residente puede reservar una zona."*
Un requisito **no funcional** dice: *"La operación debe responder en determinado tiempo."*

**RNF-01 — Rendimiento**
El sistema deberá responder a las operaciones normales de consulta y registro en un tiempo máximo de 2 segundos bajo una carga normal definida para el MVP.

**RNF-02 — Seguridad**
El sistema deberá restringir el acceso a funcionalidades según el rol autenticado del usuario. El sistema utiliza autenticación por roles dentro del alcance planteado.

## Flujo trivial de extremo a extremo

```
Frontend → Backend → Base de datos → Backend → Frontend
```

Caso: **"Registrar una PQRS"**

## Primera prueba unitaria (TDD)

```
crearPQRS()
estado = "PENDIENTE"
```

La prueba debe:
1. Ejecutarse.
2. Fallar inicialmente si todavía no existe la funcionalidad (**RED**).
3. Implementar la funcionalidad.
4. Ejecutarse nuevamente.
5. Pasar (**GREEN**).
6. Refactorizar si aplica (**REFACTOR**).

## Stack confirmado (activa el problema duro)

```
React → Express → Prisma → PostgreSQL
```
