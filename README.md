## Housing Control

Sistema de gestión residencial diseñado para administrar Peticiones, Quejas, Reclamos y Sugerencias (PQRS), reservas de zonas comunes y control de visitantes.

## Stack Tecnológico

*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 3.x
*   **Persistencia:** Spring Data JPA (Hibernate)
*   **Base de Datos:** PostgreSQL
*   **Infraestructura:** Docker & Docker Compose

## Prerrequisitos

Para ejecutar este proyecto localmente, necesitas instalar:
*   [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) Versión 21 configurado en tus variables de entorno.
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) para levantar el contenedor de la base de datos automáticamente.

## Instalación y Ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/lcardona3815-ship-it/houstingcontrol-software.git
   cd housingcontrol-software
   git checkout feature/esqueleto-inicial
   ```

2. Levanta la base de datos:
   ```bash
   docker-compose up -d
   ```

3. Inicia el servidor backend:
   ```bash
   cd backend/walkin-skeleton
   ./mvnw spring-boot:run
   ```
   Desde PowerShell
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   El servidor iniciará localmente y estará disponible en http://localhost:8080

## Documentación
Toda la documentación técnica y de negocio se encuentra estructurada en las siguientes carpetas:
- [`docs/vision-producto.md`](./docs/vision-producto.md) — Problema, usuarios, propuesta de valor y alcance del MVP.
- [`docs/problema-duro.md`](./docs/problema-duro.md) — Reto técnico, invariante de negocio y evidencia exigida.
- [`docs/historias-usuario.md`](./docs/historias-usuario.md) — Historias de usuario con criterios de aceptación.
- [`docs/uso-ia.md`](./docs/uso-ia.md) — Política de uso de IA y bitácora del equipo.

## Historias de usuario implementadas

### HU-04 — Registrador de correspondencia

**Como** portero, **quiero** registrar un paquete recibido **para** informar al residente y mantener trazabilidad.

**Qué hace:**
- Registra el destinatario, que siempre debe ser un usuario registrado.
- Registra la descripción del paquete.
- Registra la fecha de recepción.
- Asocia el paquete a un estado: `RECIBIDO → NOTIFICADO → ENTREGADO`.

**Pruebas:**
- Unitarias: `backend/walkin-skeleton/software/src/test/java/com/housingcontrol/software/application/service/CorrespondenciaServiceTest.java`
- Integración: `backend/walkin-skeleton/software/src/test/java/com/housingcontrol/software/infrastructure/controller/`

**Cómo ejecutar las pruebas:**
```bash
cd backend/walkin-skeleton/software
mvn test
```

**Commit de la implementación:** [`108c6f7`](https://github.com/lcardona3815-ship-it/houstingcontrol-software/commit/108c6f7096dd7d52105caa624d6c8f736b72eb49)

**Trazabilidad: criterio de aceptación → prueba**

| Criterio de aceptación | Prueba que lo verifica |
|---|---|
| Se registra destinatario | `registraPaqueteConEstadoRecibidoYFechaDeRecepcion` |
| Se rechaza si el destinatario no existe | `rechazaSiElDestinatarioNoExiste` |
| Se exige la descripción | `rechazaSiFaltaLaDescripcion` |
| Se registra fecha de recepción | `registraPaqueteConEstadoRecibidoYFechaDeRecepcion` |
| Estado `RECIBIDO → NOTIFICADO` | `notificaPaqueteRecibidoYPasaANotificado` |
| Estado `NOTIFICADO → ENTREGADO` | `entregaPaqueteNotificadoYPasaAEntregado` |
| No se salta de estado | `rechazaEntregarSiElPaqueteNoFueNotificado` |

**Resultado de la ejecución** (`./mvnw test`): 35 pruebas del proyecto, 0 fallos, `BUILD SUCCESS`.
De ellas, 23 son de la HU-04: `CorrespondenciaServiceTest` (3), `CorrespondenciaEstadoServiceTest` (5), `CorrespondenciaControllerTest` (2), `CorrespondenciaEstadoControllerTest` (6), `CorrespondenciaIntegrationTest` (3) y `CorrespondenciaEstadoIntegrationTest` (4).

Evidencia completa: [`docs/evidencia-hu04.txt`](docs/evidencia-hu04.txt)

**Endpoints de cambio de estado**

| Endpoint | Resultado | Prueba que lo verifica |
|---|---|---|
| `PATCH /api/correspondencias/{id}/notificar` | `200` y estado `NOTIFICADO` | `notificarPaqueteValidoResponde200` |
| `PATCH /api/correspondencias/{id}/entregar` | `200` y estado `ENTREGADO` | `entregarPaqueteValidoResponde200` |
| Paquete inexistente | `404` | `notificarPaqueteInexistenteResponde404` |
| Salto de estado inválido | `409` | `entregarSinHaberNotificadoResponde409` |

**Prueba de integración del flujo completo** (HTTP → servicio → JPA → H2, sin mocks)

| Verificación | Prueba |
|---|---|
| Registrar, notificar y entregar; el estado queda guardado en cada paso | `flujoCompletoRecibidoNotificadoEntregadoQuedaGuardadoEnBaseDeDatos` |
| Entregar sin notificar → `409` y el estado sigue `RECIBIDO` | `entregarSinNotificarResponde409YElEstadoSigueRecibido` |
| Notificar dos veces → `409` y el estado sigue `NOTIFICADO` | `notificarDosVecesResponde409YElEstadoSigueNotificado` |
| Paquete inexistente → `404` | `cambiarEstadoDePaqueteInexistenteResponde404` |
