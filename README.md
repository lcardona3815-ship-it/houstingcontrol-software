## Housing Control

Sistema de gestión residencial diseñado para administrar Peticiones, Quejas, Reclamos y Sugerencias (PQRS), reservas de zonas comunes y control de visitantes.

## Stack Tecnológico

*   **Lenguaje:** Java 21
*   **Framework:** Spring Boot 4.x
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
   git checkout main
   ```

2. Levanta la base de datos:
   ```bash
   docker-compose up -d
   ```

3. Inicia el servidor backend:
   
   Navegar a la ruta C:\Users\Sebas\Desktop\Proyecto\houstingcontrol-software\backend\software y ejecutar SoftwareApplication.java
    
   ```bash
   cd backend/software
    # 1. Empaquetamos tus credenciales reales
    $credenciales = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("1010000001:LA_CONTRASENA_REAL"))

    # 2. Configuramos las cabeceras con el pase de seguridad correcto
    $headers = @{
    "Content-Type" = "application/json; charset=utf-8"
    "Authorization" = "Basic $credenciales"
    }
   ```
   El servidor iniciará localmente y estará disponible en http://localhost:8080

## Pruebas funcionales

Para probar la creación de una PQRS de extremo a extremo, asegúrate de tener al menos un usuario registrado en tu base de datos y ejecuta el siguiente comando en PowerShell:

```powershell
#Crear una PQRS
Invoke-RestMethod -Uri http://localhost:8080/api/pqrs -Method POST -Headers $headers -Body '{"asunto": "Fallo electrico", "descripcion": "Lampara del pasillo fundida", "cedulaUsuarios": "1010000001"}'

#Consultar las PQRS
Invoke-RestMethod -Uri http://localhost:8080/api/pqrs/mis-solicitudes -Method GET -Headers $headers
```

Respuesta exitosa esperada (HTTP 201 Created):

```json
{
    asunto         : Fallo electrico
    descripcion    : Lampara del pasillo fundida
    cedulaUsuarios : 1010000001
    estado         : PENDIENTE
    id             : 05b2d2ce-84ce-4c12-85c2-3d5a6d61c196
    respuesta      :
}
```

## Documentación

Toda la documentación técnica y de negocio se encuentra estructurada en las siguientes carpetas:
- [`docs/vision-producto.md`](./docs/vision-producto.md) — Problema, usuarios, propuesta de valor y alcance del MVP.
- [`docs/problema-duro.md`](./docs/problema-duro.md) — Reto técnico, invariante de negocio y evidencia exigida.
- [`docs/historias-usuario.md`](./docs/historias-usuario.md) — Historias de usuario con criterios de aceptación.
- [`docs/uso-ia.md`](./docs/uso-ia.md) — Política de uso de IA y bitácora del equipo.
- [`docs/rebanada.md`](/docs/rebanada.md) - Esqueleto Andante: Diagramas de secuencia, clases y trazabilidad técnica.
- [`docs/models`](/docs/models) - Modelo de la base de datos y modelo de dominio.
- [`docs/deuda-tecnica.md`](/docs/deuda-tecnica.md) - Planteamiento de la deuda técnica del proyecto.
- [`docs/decisiones-tecnicas.md`](/docs/decisiones-tecnicas.md) - Documentación de las decisiones fuertes tomadas en el trayecto del proyecto.
- [`docs/informe-tecnico.md`](/docs/informe-tecnico.md) - Descripción de la implementación de las historias de usuario.
- [`docs/paquete-entrevista.md`](/docs/paquete-entrevista.md) - Entrevistas realizadas.
- [`docs/seguridad-y-rnf.md`](/docs/informe-tecnico.md) - Descripción de la implementación de las historias de usuario.
- [`database`](/database) - Scripts DDL e instrucciones de inserción de datos para pruebas.
