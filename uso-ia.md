# Declaración de Uso Responsable de IA Generativa — "Control Housing Estates"

## Política Inicial del Equipo

* La IA se utiliza como tutor para aclarar conceptos, revisar consistencia entre documentos, generar plantillas y proponer estructura de código y de pruebas.
* Ningún código o prueba generado por IA se fusiona a `main` sin haber sido ejecutado, comprendido y verificado por un integrante del equipo.
* Cada integrante debe ser capaz de defender oralmente cualquier línea de código o decisión asociada a su nombre.
* La IA no decide requisitos, historias de usuario, criterios de aceptación ni asignación de permisos por rol — esas son decisiones del equipo, incluso cuando la IA propone una plantilla o estructura inicial.

---

## Registro de Uso de IA — Semana 3 (Alcance y documentación)

| Pregunta / Campo | Respuesta Registrada |
|---|---|
| **1. ¿Qué herramienta se usó?** | Claude (Sonnet). |
| **2. ¿Para qué se usó?** | Revisar la consistencia entre los Objetivos Específicos del documento inicial (que incluían "gestión de mantenimientos") y el alcance del MVP declarado (que lo excluía), tras una revisión de un tutor. |
| **3. ¿Qué se aceptó?** | La estructura de una tabla explícita "Visión completa del producto vs. MVP", que deja clara la decisión de dejar mantenimientos fuera del MVP como evolución futura. |
| **4. ¿Qué se modificó?** | Se ajustó la redacción para que quedara explícita la nota de consistencia, en vez de dejarlo implícito como estaba antes. |
| **5. ¿Qué se rechazó?** | La IA se abstuvo explícitamente de redactar las historias de usuario y sus criterios de aceptación (aunque se le pidió ayuda relacionada), porque esa decisión le corresponde al equipo defenderla; el equipo las redactó por su cuenta y luego se le pidió a la IA solo darles formato. |
| **6. ¿Cómo se verificó?** | Revisión manual por el equipo, comparando la tabla contra el documento original del proyecto y contra las observaciones de la revisión del tutor. |
| **7. ¿Cuál fue la decisión humana final?** | Adoptar la separación explícita visión/MVP y mantener "gestión de mantenimientos" fuera del MVP. |
| **8. ¿Qué riesgo permanece?** | Persisten datos provisionales en el README (integrantes, cronograma, número real de semanas del curso) que el equipo aún debe confirmar y cerrar. Se registra como pendiente. |

## Registro de Uso de IA — Semana 4 (Esqueleto ejecutable)

| Pregunta / Campo | Respuesta Registrada |
|---|---|
| **1. ¿Qué herramienta se usó?** | Claude (Sonnet). |
| **2. ¿Para qué se usó?** | Proponer la estructura inicial del backend (Express + Prisma) y una primera prueba unitaria para el flujo trivial "Registrar una PQRS". |
| **3. ¿Qué se aceptó?** | El diseño de `crearPQRS()` con almacenamiento en memoria, el endpoint `POST /pqrs`, y las pruebas unitaria/integración correspondientes. |
| **4. ¿Qué se modificó?** | *(pendiente — a completar por el equipo tras correr `npm install && npm test` en su propio entorno y ajustar lo necesario)*. |
| **5. ¿Qué se rechazó?** | Aún no se implementó la solución real de concurrencia para el problema duro (reservas duplicadas de zonas comunes, equivalente al control de hilos en otros dominios) — se dejó explícitamente pendiente en vez de aceptar una implementación no verificada. |
| **6. ¿Cómo se verificó?** | No fue posible ejecutar `npm install`/`npm test` desde el entorno de la IA (sin acceso a red); queda pendiente que el equipo lo ejecute y registre el resultado real (rojo→verde) aquí. |
| **7. ¿Cuál fue la decisión humana final?** | Adoptar el esqueleto en memoria como punto de partida y migrar a persistencia real con Prisma/PostgreSQL en la Semana 5, según el cronograma del curso. |
| **8. ¿Qué riesgo permanece?** | La técnica para garantizar el invariante de negocio en reservas duplicadas (declarado en `problema-duro.md`) todavía no está implementada ni probada — se registra como **Deuda Técnica (DT-01)**. |

## Registro de Uso de IA — Semana [N]

| Pregunta / Campo | Respuesta Registrada |
|---|---|
| **1. ¿Qué herramienta se usó?** | |
| **2. ¿Para qué se usó?** | |
| **3. ¿Qué se aceptó?** | |
| **4. ¿Qué se modificó?** | |
| **5. ¿Qué se rechazó?** | |
| **6. ¿Cómo se verificó?** | |
| **7. ¿Cuál fue la decisión humana final?** | |
| **8. ¿Qué riesgo permanece?** | |
