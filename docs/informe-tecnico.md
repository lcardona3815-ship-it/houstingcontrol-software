# Informe Técnico — Housing Control

> Ingeniería de Software (702301) · Módulo 8 – Semana 15
> Repositorio: `houstingcontrol-software` · Rama: `feature/esqueleto-inicial`
> Autor(es): ⚠️ COMPLETAR · Fecha: 11 de noviembre de 2026

> Los puntos marcados con ⚠️ son datos que solo el equipo puede confirmar. Reemplázalos o bórralos antes del Pull Request.

---

## 1. Resumen ejecutivo

**Housing Control** es un sistema de gestión residencial para conjuntos y propiedades horizontales. Centraliza la atención de Peticiones, Quejas, Reclamos y Sugerencias (PQRS), las reservas de zonas comunes, el control de visitantes y el registro de correspondencia.

**Problema que resuelve.** La gestión residencial suele hacerse con papel, chats y hojas de cálculo: las solicitudes no tienen trazabilidad, las reservas de zonas comunes pueden duplicarse y los paquetes o visitantes se pierden sin registro.

**Estado actual del producto.**

| Funcionalidad | Estado |
|---|---|
| Registro de PQRS (esqueleto andante) | Implementada con pruebas |
| Login y registro de visitantes (HU-01, HU-02) | Implementadas con pruebas |
| Registro de correspondencia y flujo de estados (HU-04) | Implementada con pruebas unitarias e integración |
| Reservas de zonas comunes (HU-06, HU-07) | Implementadas con pruebas |
| HU-03, HU-05, HU-08 | Documentadas en `docs/historias-usuario.md`. ⚠️ Confirmar cuáles tienen código (el sistema expone además listar, "mis solicitudes" y actualizar PQRS) |

**Resultado verificable:** `./mvnw test` en `backend/software` → **52 pruebas, 0 fallos, 0 errores, `BUILD SUCCESS`** (11 de noviembre de 2026).

**Seguridad.** El proyecto pasó por una auditoría y una remediación de vulnerabilidades, documentadas en `docs/seguridad-y-rnf.md` y `docs/remediación.md`.

---

## 2. Arquitectura y diseño

### 2.1 Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot (versión en `backend/software/pom.xml`) |
| Persistencia | Spring Data JPA con Hibernate |
| Base de datos | PostgreSQL con Docker Compose; H2 en memoria para demostración y pruebas |
| Seguridad | Spring Security (codificación de contraseñas en `PasswordConfig`) |
| Pruebas | JUnit, Mockito, Spring Boot Test / MockMvc |
| Construcción | Maven Wrapper (`mvnw`) |

### 2.2 Estilo arquitectónico

Arquitectura en capas bajo el paquete base `com.housingcontrol.software`:

```
domain/          Entidades y reglas: Usuario, Rol, Pqrs, Reserva, Visitante,
                 Correspondencia, EstadoPaquete
application/     Casos de uso: AuthService, PqrsService, ReservaService,
                 VisitanteService, CorrespondenciaService y sus DTOs
infrastructure/  Controladores REST, repositorios JPA y configuración
```

Flujo de una petición: `Controller → Service → Repository → Base de datos`. La lógica de negocio vive en los servicios y el dominio, por lo que se prueba con Mockito sin levantar Spring.

### 2.3 Modelo de datos y de dominio

- Modelo de dominio: `docs/models/modelo-dominio.md`
- Modelo de datos: `docs/models/modelo-DB.md`
- Scripts DDL y datos de prueba: carpeta `database/`

La aplicación define cinco repositorios JPA: Correspondencia, Pqrs, Reserva, Usuario y Visitante.

### 2.4 Decisiones de diseño

| Decisión | Justificación |
|---|---|
| Arquitectura en capas | Separa reglas de negocio de HTTP y persistencia; facilita las pruebas |
| El destinatario de una correspondencia debe ser un usuario registrado | Integridad referencial y trazabilidad |
| Estados de correspondencia `RECIBIDO → NOTIFICADO → ENTREGADO` | No se permiten saltos; el servicio rechaza transiciones inválidas |
| Perfil `h2` para ejecutar sin Docker | Permite demostrar y probar el sistema en equipos sin virtualización |
| Evitar reservas duplicadas con una restricción `UNIQUE` en la base de datos (`unique_reserva_zona_fecha` en la entidad `Reserva`) | El bloqueo optimista (`@Version`) protege la actualización de una fila existente, pero no impide insertar dos reservas nuevas para la misma zona y fecha. La restricción única hace cumplir la regla en la base de datos aunque las peticiones lleguen a la vez |

---

## 3. Implementación

### 3.1 Estructura del repositorio

```
backend/software/        Proyecto Spring Boot vigente (código y pruebas)
backend/walkin-skeleton/ Esqueleto andante original, conservado como referencia histórica
database/                DDL y datos de prueba
docs/                    Documentación técnica y de negocio
scripts/                 Scripts de evidencia y demostración
docker-compose.yml       PostgreSQL para ejecución local
```

El proyecto vigente es `backend/software`. La carpeta `backend/walkin-skeleton` se conserva por trazabilidad del esqueleto andante y no se usa para ejecutar el sistema.

### 3.2 Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/pqrs` | Registra una PQRS en estado `PENDIENTE` |
| PATCH | `/api/correspondencias/{id}/notificar` | `RECIBIDO → NOTIFICADO` |
| PATCH | `/api/correspondencias/{id}/entregar` | `NOTIFICADO → ENTREGADO` |
| POST | `/api/auth/login` | Inicio de sesión |
| POST | `/api/correspondencias` | Registra correspondencia en estado `RECIBIDO` |
| GET | `/api/pqrs` y `/api/pqrs/mis-solicitudes` | Lista PQRS y PQRS del usuario |
| PUT | `/api/pqrs/{id}` | Actualiza una PQRS |
| POST | `/api/visitantes` | Registra un visitante |
| GET | `/api/visitantes/buscar/{cedula}` | Busca un visitante por cédula |
| PUT | `/api/visitantes/{id}/ingreso` | Registra el ingreso del visitante |
| GET | `/api/reservas/disponibilidad` | Consulta disponibilidad de zonas comunes |
| POST | `/api/reservas` | Crea una reserva |

Códigos de respuesta de HU-04: `200` en éxito, `404` si el paquete no existe y `409` si la transición de estado es inválida.

### 3.3 Historia de ejemplo: HU-04

1. El portero envía la descripción del paquete y la cédula del destinatario (`RegistrarCorrespondenciaDTO`).
2. `CorrespondenciaService` valida descripción y destinatario, busca al usuario y crea el paquete en estado `RECIBIDO` con fecha de recepción.
3. Los endpoints `notificar` y `entregar` avanzan el estado; un salto inválido produce error de negocio (`409`).

### 3.4 Flujo de trabajo

Ramas por funcionalidad (`feature/hu01-hu02`, `feature/hu04-correspondencia`, `feature/esqueleto-inicial`), integración mediante merge con resolución documentada de conflictos, y TDD en las historias implementadas. El uso de IA se registra en `docs/uso-ia.md`. ⚠️ Añadir enlaces a los Pull Requests revisados.

---

## 4. Pruebas y calidad

### 4.1 Estrategia

| Nivel | Herramienta | Alcance |
|---|---|---|
| Unitarias de servicio | JUnit + Mockito | `AuthServiceTest`, `PqrsServiceTest`, `ReservaServiceTest`, `VisitanteServiceTest`, `CorrespondenciaServiceTest`, `CorrespondenciaEstadoServiceTest` |
| De controlador | MockMvc | `AuthControllerTest`, `PqrsControllerTest`, `ReservaControllerTest`, `VisitanteControllerTest`, `CorrespondenciaControllerTest`, `CorrespondenciaEstadoControllerTest` |
| Integración | Spring Boot Test + H2 | `CorrespondenciaIntegrationTest`, `CorrespondenciaEstadoIntegrationTest` (HTTP → servicio → JPA → H2, sin mocks) |
| Arranque | Spring Boot Test | `SoftwareApplicationTests` (carga del contexto) |

### 4.2 Resultado

```
Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Cómo reproducirlo:

```bash
cd backend/software
./mvnw test          # Linux / macOS / Git Bash
.\mvnw.cmd test      # PowerShell
```

Evidencia de HU-04: `docs/evidencia-hu04.txt`, `docs/evidencia-hu04-demo.txt` y el script `scripts/evidencia-hu04.sh`.

### 4.3 Trazabilidad (HU-04)

| Criterio de aceptación | Prueba |
|---|---|
| Se registra destinatario y fecha de recepción | `registraPaqueteConEstadoRecibidoYFechaDeRecepcion` |
| Se rechaza si el destinatario no existe | `rechazaSiElDestinatarioNoExiste` |
| Se exige destinatario y descripción | `rechazaSiFaltaElDestinatario`, `rechazaSiFaltaLaDescripcion` |
| `RECIBIDO → NOTIFICADO` | `notificaPaqueteRecibidoYPasaANotificado` |
| `NOTIFICADO → ENTREGADO` | `entregaPaqueteNotificadoYPasaAEntregado` |
| No se salta de estado | `rechazaEntregarSiElPaqueteNoFueNotificado` |
| Flujo completo persistido | `flujoCompletoRecibidoNotificadoEntregadoQuedaGuardadoEnBaseDeDatos` |

Cobertura por funcionalidad:

| Funcionalidad | Pruebas |
|---|---|
| Login | `AuthServiceTest`, `AuthControllerTest` |
| PQRS | `PqrsServiceTest`, `PqrsControllerTest` |
| Visitantes | `VisitanteServiceTest`, `VisitanteControllerTest` |
| Reservas | `ReservaServiceTest`, `ReservaControllerTest` |
| Correspondencia | Ver tabla anterior y `CorrespondenciaIntegrationTest` |

### 4.4 Calidad y seguridad

- La auditoría y la remediación de vulnerabilidades están en `docs/seguridad-y-rnf.md` y `docs/remediación.md`.
- En el código fuente no hay una configuración de seguridad propia (no existe un `SecurityFilterChain`): Spring Security arranca con su configuración por defecto y muestra el aviso `Using generated security password`. Existen el login (`/api/auth/login`) y la codificación de contraseñas (`PasswordConfig`), pero la autorización por rol y la gestión de sesión o token quedan como deuda. Esto debe contrastarse con lo descrito en `docs/remediación.md`.
- Otro aviso del arranque: `spring.jpa.open-in-view` está activo por defecto; conviene desactivarlo explícitamente.

### 4.5 Deuda técnica

Detalle completo y plan de mejora priorizado en [`deuda-tecnica.md`](deuda-tecnica.md). Puntos a resaltar en la defensa:

| Área | Mejora | Impacto | Esfuerzo |
|---|---|---|---|
| Seguridad | Definir una configuración propia (`SecurityFilterChain`) con autorización por rol | Alto | Medio |
| Configuración | Desactivar `open-in-view` | Medio | Bajo |
| Estructura | Retirar o archivar `backend/walkin-skeleton` y archivos de trabajo de la raíz | Bajo | Bajo |
| Pruebas | Probar contra PostgreSQL real (Testcontainers) y alinear el esquema de pruebas con `v1-initial-schema.sql` | Medio | Medio |
| Concurrencia | `@Version` en `Correspondencia` y prueba con peticiones concurrentes | Bajo | Bajo |
| Errores | Excepciones propias y un manejador central (`@ControllerAdvice`) | Medio | Bajo |
| Funcional | Notificación real al residente e historial de transiciones de correspondencia | Medio | Medio |

---

## 5. Lecciones aprendidas

**Técnicas**
- Un esqueleto andante que atraviesa todas las capas reduce el riesgo de integración.
- Escribir la prueba antes del código obliga a aclarar el criterio de aceptación.
- Separar dominio, servicios e infraestructura permite probar reglas de negocio con Mockito sin base de datos.
- Un perfil con H2 en memoria permite demostrar el sistema incluso sin Docker.
- Una prueba que solo comprueba el tipo de excepción puede pasar por la razón equivocada: `rechazaSiFaltaLaDescripcion` seguía en verde aunque se borrara la validación, y se corrigió comprobando también el mensaje.

**De proceso**
- Unificar el código en una sola ruta de proyecto evita duplicados; hacerlo tarde generó conflictos de merge en el `README.md`, en el `pom.xml` y en documentos.
- La trazabilidad historia → criterio → prueba → evidencia acelera las revisiones.
- Un README que no se prueba desde cero puede traer errores sutiles (por ejemplo, una ruta o nombre de carpeta incorrecto).
- Los artefactos de trabajo (parches, bundles, zips) no deberían quedar en la raíz del repositorio.

**De equipo**
- ⚠️ Añadir 2 o 3 lecciones propias: reparto de trabajo, revisión cruzada, uso crítico de IA (ver `docs/uso-ia.md`).

**Qué haríamos con más tiempo**
- Completar la seguridad de ejecución, cubrir con pruebas las historias restantes y automatizar las pruebas en un pipeline de CI.

---

## 6. Referencias

- [README](../README.md)
- [Visión del producto](vision-producto.md) · [Problema duro](problema-duro.md) · [Historias de usuario](historias-usuario.md)
- [Rebanada](rebanada.md) · [Uso de IA](uso-ia.md)
- [Modelo de dominio](models/modelo-dominio.md) · [Modelo de datos](models/modelo-DB.md)
- [Seguridad y RNF](seguridad-y-rnf.md) · [Remediación](remediaci%C3%B3n.md)
- [Guía técnica HU-04](guia-tecnica-hu04.md) · [Deuda técnica](deuda-tecnica.md)
