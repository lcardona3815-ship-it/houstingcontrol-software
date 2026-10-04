# Deuda técnica — HU-04 Registrador de correspondencia

Registro de las limitaciones conocidas de la implementación actual, con su impacto y el plan para resolverlas.

## 1. Demo con Docker y PostgreSQL no ejecutada

- **Qué pasa:** el `docker-compose.yml` y el script `database/v1-initial-schema.sql` existen, pero el equipo de desarrollo no tiene virtualización habilitada, así que no se pudo levantar el contenedor de PostgreSQL.
- **Impacto:** las pruebas corren contra H2, no contra PostgreSQL real.
- **Plan:** ejecutar la demo en un equipo con virtualización, o con una instalación local de PostgreSQL, y guardar la evidencia (captura de `docker compose up` y de una consulta a la tabla `correspondencias`).

## 2. El esquema de las pruebas no es el esquema SQL del proyecto

- **Qué pasa:** en las pruebas, `spring.jpa.hibernate.ddl-auto=create-drop` hace que Hibernate genere las tablas a partir de las entidades. El script `v1-initial-schema.sql` no se ejecuta en las pruebas.
- **Impacto:** si el script SQL y las entidades se desalinean (nombres de columnas, restricciones), las pruebas siguen pasando sin detectarlo.
- **Plan:** agregar una prueba de integración con Testcontainers y PostgreSQL que cargue el script real, o validar el esquema con `ddl-auto=validate`.

## 3. Pruebas del controlador con mocks

- **Qué pasa:** `CorrespondenciaControllerTest` y `CorrespondenciaEstadoControllerTest` llaman al controlador directamente con el servicio simulado.
- **Impacto:** verifican la traducción de excepciones a códigos HTTP, pero no el enrutamiento, la serialización JSON ni las anotaciones.
- **Mitigación actual:** `CorrespondenciaIntegrationTest` y `CorrespondenciaEstadoIntegrationTest` recorren el flujo HTTP completo con `MockMvc` y la base H2.

## 4. La notificación no avisa al residente

- **Qué pasa:** `notificar` solo cambia el estado a `NOTIFICADO`. No se envía ningún mensaje real (correo, SMS, notificación en la aplicación).
- **Impacto:** la historia dice "para informar al residente"; hoy eso se refleja como un estado, no como un aviso efectivo.
- **Plan:** definir un puerto de notificación en la capa de aplicación (por ejemplo `NotificadorResidente`) con una implementación de correo o registro en bitácora.

## 5. Sin control de concurrencia en el cambio de estado

- **Qué pasa:** `Correspondencia` no tiene campo de versión. Dos peticiones simultáneas sobre el mismo paquete podrían leer el mismo estado y ambas aplicar la transición.
- **Impacto:** bajo para este caso de uso, pero sin garantía.
- **Plan:** agregar `@Version` (bloqueo optimista) y una prueba con peticiones concurrentes, como ya se plantea para las reservas (HU-06).

## 6. Sin historial de transiciones

- **Qué pasa:** el paquete guarda solo su estado actual y la fecha de recepción. No registra quién notificó o entregó, ni cuándo.
- **Impacto:** limita la trazabilidad que pide la historia ("mantener trazabilidad").
- **Plan:** tabla de eventos de correspondencia (paquete, estado anterior, estado nuevo, usuario, fecha).

## 7. Diseño del modelo

- **Estado como texto:** `Correspondencia.estado` es un `String` y el enum `EstadoPaquete` se usa solo para producir y comparar el valor. Un valor inválido en la base no se detectaría al leer.
- **Nombre del destinatario duplicado:** `nombreDestinatario` copia el nombre del usuario al registrar. Si el usuario cambia de nombre, el paquete conserva el anterior. Puede ser intencional (foto del momento de recepción), pero conviene decidirlo y documentarlo.
- **Manejo de errores repetido:** cada endpoint traduce excepciones con `try/catch`. Un `@ControllerAdvice` evitaría la repetición.
- **Códigos distintos para el mismo error:** un destinatario inexistente responde `400` al registrar, y un paquete inexistente responde `404` al cambiar de estado. Es coherente (uno es un dato del cuerpo, el otro un recurso en la ruta), pero hay que poder explicarlo.

## 8. Por verificar

- Si el script SQL define una restricción `CHECK` o un tipo enumerado para `estado`.
- Si los endpoints de cambio de estado deben restringirse al rol de portería. Esta revisión no incluyó seguridad.
