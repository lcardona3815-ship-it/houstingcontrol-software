# Paquete de evidencia de entrevista — Housing Control

> Documento de preparación para la defensa técnica (Módulo 8 – Semana 15).
> Las respuestas son un borrador basado en el repositorio. **Reescríbelas con tus palabras**: anticipar no es memorizar, es comprender. Los puntos con ⚠️ requieren que los confirmes.

---

## 1. El proyecto en un minuto

Housing Control es un sistema de gestión residencial que centraliza PQRS, reservas de zonas comunes, control de visitantes y registro de correspondencia. Está construido con Java 21, Spring Boot y JPA, en arquitectura de capas (`domain`, `application`, `infrastructure`). Se puede ejecutar con PostgreSQL en Docker o con una base H2 en memoria, y tiene 52 pruebas automatizadas en verde.

**Qué lo hace defendible:** cada historia tiene criterios de aceptación, pruebas que los verifican y evidencia reproducible.

---

## 2. Decisiones importantes y justificación

| Decisión | Por qué | Alternativa y motivo de descarte |
|---|---|---|
| Arquitectura en capas | Separa reglas de negocio de HTTP y persistencia; permite probar servicios con Mockito sin levantar Spring | Lógica en controladores: más rápida de escribir, pero difícil de probar y mantener |
| Restricción `UNIQUE` para evitar reservas duplicadas (`unique_reserva_zona_fecha`) | Garantiza la regla en la base de datos aunque las peticiones lleguen al mismo tiempo | `@Version` (bloqueo optimista): protege actualizaciones de una fila existente, pero no impide insertar dos reservas nuevas para la misma zona y fecha |
| Correspondencia con estados `RECIBIDO → NOTIFICADO → ENTREGADO` | Controla el ciclo de vida del paquete y evita saltos | Estado libre: permitiría entregar sin avisar |
| Destinatario = usuario registrado | Integridad y trazabilidad | Nombre libre: se pierde la relación con el residente |
| Perfil `h2` además de PostgreSQL | El equipo no tenía virtualización para Docker; permite demostrar y probar | Solo PostgreSQL: bloquea la ejecución en equipos sin Docker |
| Pruebas en varios niveles (servicio, controlador, integración) | Cada nivel detecta un tipo distinto de error | Solo pruebas unitarias: no detectan fallos de enrutamiento ni persistencia |
| Un único proyecto vigente en `backend/software` | Evita duplicados y confusión | Mantener dos copias activas |

---

## 3. Evidencia técnica

| Qué se demuestra | Dónde está | Cómo verificarlo |
|---|---|---|
| Pruebas en verde | `backend/software/src/test` | `./mvnw test` → 52 pruebas, 0 fallos, `BUILD SUCCESS` |
| Trazabilidad HU-04 | `README.md`, `docs/guia-tecnica-hu04.md` | Tabla criterio → prueba |
| Flujo completo HTTP → JPA → H2 | `CorrespondenciaEstadoIntegrationTest` | Se ejecuta con `./mvnw test` |
| Demo reproducible | `scripts/evidencia-hu04.sh`, `docs/evidencia-hu04-demo.txt` | `bash scripts/evidencia-hu04.sh` |
| Seguridad y remediación | `docs/seguridad-y-rnf.md`, `docs/remediación.md` | Lectura de los documentos |
| Deuda técnica | `docs/deuda-tecnica.md` | Lectura del documento |
| Instalación desde cero | `docs/guia-instalacion.md` | Seguir la guía en un equipo limpio |

---

## 4. Preguntas de defensa y respuestas base

**¿Por qué tomaron esta decisión de diseño?**
Elegimos capas para separar las reglas de negocio de la infraestructura. Así los servicios se prueban con Mockito sin base de datos, y cambiar el controlador o el repositorio no obliga a tocar la lógica.

**¿Qué problemas encontraron y cómo los resolvieron?**
- *Sin virtualización para Docker:* añadimos el perfil `h2` para ejecutar y probar sin contenedores.
- *Prueba que pasaba por la razón equivocada:* `rechazaSiFaltaLaDescripcion` seguía en verde aunque se borrara la validación, porque otra excepción del mismo tipo la disparaba. Lo corregimos comprobando el mensaje.
- *Conflictos al unir ramas:* el código estaba en dos rutas (`walkin-skeleton` y `software`). Unificamos en `backend/software` y resolvimos los conflictos de `README.md` y `pom.xml`.

**¿Qué alternativas evaluaron y por qué no las eligieron?**
Para reservas duplicadas comparamos `UNIQUE` y `@Version`: el segundo no cubre inserciones simultáneas de reservas nuevas. Para los errores, hoy usamos `try/catch` por endpoint y evaluamos excepciones propias con un manejador central; quedó como mejora.

**¿Cuáles son los riesgos actuales del sistema?**
- Las pruebas corren contra H2, no contra PostgreSQL real, y el esquema de pruebas lo genera Hibernate, no el script SQL.
- No hay una configuración de seguridad propia (`SecurityFilterChain`): la autorización por rol queda pendiente. ⚠️ Contrastar con `docs/remediación.md`.
- La notificación de correspondencia solo cambia el estado; no envía ningún aviso real.
- `Correspondencia` no tiene control de concurrencia ni historial de transiciones.

**¿Qué harían si tuvieran más tiempo?**
Seguir el plan de `docs/deuda-tecnica.md`: seguridad por rol, pruebas con Testcontainers y PostgreSQL, excepciones propias con `@ControllerAdvice`, notificación real al residente e historial de estados.

**¿Cómo garantizan la calidad del software?**
Con TDD en las historias, 52 pruebas en tres niveles, trazabilidad entre criterio de aceptación y prueba, revisión por Pull Request y una guía de instalación verificable.

**¿Qué aprendieron como equipo?**
Que unificar el código pronto evita conflictos tardíos, que una prueba debe fallar por la razón correcta y que documentar mientras se construye cuesta menos que hacerlo al final. ⚠️ Añade una lección propia del equipo.

---

## 5. Preguntas técnicas de conocimiento

| Pregunta | Respuesta corta |
|---|---|
| ¿Diferencia entre prueba unitaria y de integración? | La unitaria aísla una clase con dependencias simuladas (Mockito); la de integración recorre varias capas reales: HTTP → servicio → JPA → H2 |
| ¿Por qué `404`, `409` y `400`? | `404`: el recurso de la ruta no existe; `409`: el estado actual no permite la transición; `400`: el dato del cuerpo es inválido |
| ¿Qué hace el perfil `h2`? | Arranca con una base en memoria y usuarios de demostración, sin Docker |
| ¿`UNIQUE` frente a `@Version`? | `UNIQUE` impide filas duplicadas; `@Version` detecta que alguien modificó una fila antes que tú |
| ¿Por qué Mockito en los servicios? | Para probar reglas de negocio sin base de datos ni servidor |
| ¿Qué es un DTO y para qué se usa? | Objeto que define los datos de entrada o salida de la API, sin exponer las entidades |

### Casos de pregunta tipo "¿qué pasa si…?"

- **…dos residentes reservan la misma zona a la misma hora?** La restricción `UNIQUE` en la base de datos rechaza la segunda reserva. ⚠️ Confirmar cómo responde la API en ese caso.
- **…portería entrega un paquete sin notificar?** El servicio lo rechaza y responde `409`; el estado sigue en `RECIBIDO` (lo verifica `entregarSinNotificarResponde409YElEstadoSigueRecibido`).
- **…el destinatario no existe?** Se rechaza el registro con `400`.
- **…cae la base de datos?** ⚠️ Preparar una respuesta: qué muestra la API y qué parte del plan de mejora lo cubre.

---

## 6. Guion de demo (5–7 minutos)

**Preparación (antes de la defensa)**
- Ambiente limpio: cerrar otras aplicaciones y liberar el puerto 8080.
- Arrancar con `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2` y confirmar que levanta.
- Tener listas las peticiones (PowerShell o Postman) y los datos de prueba.
- Ensayar la demo completa al menos una vez antes del día de la defensa.

**Secuencia**
1. **Problema y usuarios** (30 s): por qué existe el sistema.
2. **Solución funcional** (3 min): login → crear una PQRS → registrar correspondencia → `notificar` → `entregar` → mostrar el error `409` al saltarse un estado. ⚠️ Probar que cada petición funciona con la configuración de seguridad actual.
3. **Calidad y pruebas** (1 min): ejecutar `./mvnw test` y mostrar las 52 pruebas en verde.
4. **Beneficios y deuda** (1 min): qué resuelve y qué queda pendiente, con `docs/deuda-tecnica.md`.

**Qué mostrar:** el flujo feliz, un error controlado y las pruebas.
**Qué no mostrar:** logs largos, configuración interna ni funciones sin probar.

La demo no es marketing: es evidencia técnica.

---

## 7. Enlaces de apoyo

- [README](../README.md) · [Guía de instalación](guia-instalacion.md) · [Informe técnico](informe-tecnico.md)
- [Guía técnica HU-04](guia-tecnica-hu04.md) · [Deuda técnica](deuda-tecnica.md)
- [Seguridad y RNF](seguridad-y-rnf.md) · [Uso de IA](uso-ia.md)
