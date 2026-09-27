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
- [`docs/rebanada.md`](/docs/rebanada.md) - Esqueleto Andante: Diagramas de secuencia, clases y trazabilidad técnica.
- [`database`](/database) - Scripts DDL e instrucciones de inserción de datos para pruebas.
