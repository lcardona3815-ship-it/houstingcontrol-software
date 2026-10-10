# Guía del proyecto (Housing Control) — HU-04 Registrador de correspondencia

*El stack, la estructura, cómo ejecutarlo y cómo leer sus pruebas, aplicado a HU-04.*

**Para quién.** Para quien abre el repositorio y quiere ubicar dónde vive HU-04, cómo se ejecuta y cómo se prueba, sin conocer Java con Spring Boot.

**La historia** (`docs/historias-usuario.md`). *Como portero, quiero registrar un paquete recibido para informar al residente y mantener trazabilidad.*

| Criterio de aceptación | Cómo lo cumple el código |
|---|---|
| Se registra destinatario | Siempre es un usuario registrado (`cedula_usuarios`); si no existe, 400 |
| Se registra descripción | Es obligatoria; si falta, 400 |
| Se registra fecha de recepción | La asigna el servicio con la hora actual |
| Estado `RECIBIDO → NOTIFICADO → ENTREGADO` | Nace `RECIBIDO`; solo se avanza en ese orden; un salto responde 409 |

**Todos los comandos de esta guía se ejecutan desde `backend/walkin-skeleton/software/`**, la carpeta donde está el `pom.xml`.

> Empieza siempre por `./mvnw verify`. Si las **36 pruebas** quedan en verde, el entorno está listo y puedes seguir.

---

## 1. El stack: qué es cada pieza y por qué está

| Pieza | Versión | Qué resuelve | Qué hace en HU-04 |
|---|---|---|---|
| Java | 21 (LTS) | el lenguaje. Si tu JDK es anterior, el proyecto no compila | todo el código |
| Maven (wrapper) | 3.9.16 | descarga librerías, compila, corre pruebas y empaqueta | compila y prueba HU-04 |
| Spring Boot | 4.1.1 | arranca un servidor web embebido y conecta las piezas solas | levanta la API en `localhost:8080` |
| Spring MVC (`starter-webmvc`) | — | convierte una petición HTTP en una llamada a un método Java | `CorrespondenciaController` |
| Spring Data JPA (Hibernate) | — | convierte clases en tablas y métodos en consultas SQL | `Correspondencia` es la tabla `correspondencias` |
| PostgreSQL | 15 (Docker) | el motor real | perfil por defecto |
| H2 | — | base en memoria | perfil `h2` (demo sin Docker) y todas las pruebas |
| `spring-boot-h2console` | — | la consola web de H2 | `http://localhost:8080/h2-console` |
| `spring-security-crypto` | — | cifrado de contraseñas | lo usa el login; HU-04 no |
| JUnit 5 + Mockito + MockMvc | (starters de prueba) | ejecutan las pruebas y fabrican objetos falsos | las 24 pruebas de HU-04 |
| Docker Compose | — | levanta PostgreSQL con un comando | `docker-compose.yml` en la raíz |

Un *starter* de Spring Boot es un paquete que trae un grupo de librerías que se usan juntas, ya con versiones compatibles.

> **Lo que este proyecto todavía no tiene:** medición de cobertura (JaCoCo) ni integración continua (GitHub Actions). Hoy "funciona" se demuestra corriendo `./mvnw verify` en tu equipo.

---

## 2. El árbol de carpetas, comentado

Lo que HU-04 creó o usa está marcado con ★; lo agregado con esta guía, con ＋.

```
houstingcontrol-software/
├── README.md                          la entrada del proyecto
├── docker-compose.yml                 levanta PostgreSQL 15 con un comando
├── database/
│   ├── v1-initial-schema.sql          el esquema real de PostgreSQL (tablas, enums)
│   └── insercion-datos.sql            datos de ejemplo para PostgreSQL
├── docs/                              lo que escribió el equipo
│   ├── historias-usuario.md           ★ la historia HU-04 y sus criterios
│   ├── evidencia-hu04.txt             ★ salida de ./mvnw test (35 pruebas)
│   ├── deuda-tecnica.md               ★ limitaciones conocidas de HU-04
│   ├── guia-tecnica-hu04.md           ＋ este documento
│   └── ...                            visión, problema duro, modelos, uso de IA
├── scripts/
│   └── evidencia-hu04.sh              ＋ corre todo y deja la evidencia en un archivo
└── backend/walkin-skeleton/software/  EL PROYECTO MAVEN (aquí se ejecuta todo)
    ├── pom.xml                        qué librerías necesita
    ├── mvnw + mvnw.cmd                el "wrapper": descarga Maven si no lo tienes
    ├── src/main/java/com/housingcontrol/software/
    │   ├── SoftwareApplication.java   el punto de arranque
    │   ├── domain/                    LAS ENTIDADES: Correspondencia ★, EstadoPaquete ★, Usuario ★, Rol ★, ...
    │   ├── application/
    │   │   ├── dto/                   los cuerpos de entrada: RegistrarCorrespondenciaDTO ★
    │   │   └── service/               LAS REGLAS: CorrespondenciaService ★
    │   └── infrastructure/
    │       ├── controller/            LA PUERTA HTTP: CorrespondenciaController ★
    │       ├── repository/            EL ACCESO A DATOS: CorrespondenciaRepository ★, UsuarioRepository ★
    │       └── config/                configuración (cifrado de contraseñas)
    ├── src/main/resources/
    │   ├── application.properties     perfil por defecto: PostgreSQL
    │   ├── application-h2.properties  ＋ perfil "h2": base en memoria + consola
    │   └── data-h2.sql                ＋ 3 usuarios de demostración para ese perfil
    ├── src/test/java/...              LAS PRUEBAS, en carpetas espejo del código
    ├── src/test/resources/application.properties   configuración de las pruebas (H2)
    └── target/                        NO existe hasta que compiles. No se versiona.
```

**Por qué esas rutas.** Es convención de Maven: `src/main/java` es el código de la aplicación, `src/main/resources` la configuración, `src/test/java` el código que solo existe para probar, y `target/` todo lo que Maven genera. Si mueves un archivo fuera de su carpeta, Maven deja de verlo.

---

## 3. El `pom.xml`, bloque por bloque

`pom` es *Project Object Model*: el único archivo que Maven necesita para saber qué construir y con qué.

### 3.1 El padre
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.1</version>
</parent>
```
Hereda de un `pom` que mantiene el equipo de Spring Boot y decide las versiones de todas las dependencias. Por eso abajo no llevan número.

### 3.2 La identidad
```xml
<groupId>com.housingcontrol</groupId>
<artifactId>software</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

### 3.3 La versión de Java
```xml
<java.version>21</java.version>
```
Si tu JDK es anterior, el proyecto no compila: es el error más frecuente al importarlo.

### 3.4 Las dependencias y su *scope*

| Dependencia | Scope | Para qué |
|---|---|---|
| `spring-security-crypto` | compile | cifrar contraseñas (login) |
| `spring-boot-starter-data-jpa` | compile | entidades, repositorios y transacciones |
| `spring-boot-starter-webmvc` | compile | controladores, JSON y servidor embebido |
| `postgresql` | runtime | driver del motor real |
| `h2` | runtime | base en memoria (perfil `h2`) y para las pruebas |
| `spring-boot-h2console` | runtime | consola web de H2. **En Spring Boot 4 es un módulo aparte**: sin él, `/h2-console` da 404 |
| `spring-boot-starter-data-jpa-test` | test | utilidades de prueba para JPA |
| `spring-boot-starter-webmvc-test` | test | JUnit 5, Mockito, MockMvc; no viaja al `.jar` |

El *scope* responde **cuándo** se necesita la librería. `runtime` = al ejecutar, no al compilar. `test` = solo al probar. (`h2` estaba en `test`; pasó a `runtime` para poder ejecutar la aplicación con él.)

### 3.5 El plugin
`spring-boot-maven-plugin` empaqueta un `.jar` ejecutable y permite `./mvnw spring-boot:run`.

### 3.6 El wrapper: por qué `./mvnw` y no `mvn`
`mvnw` descarga la versión exacta de Maven del proyecto (3.9.16). Úsalo siempre, también en Windows (en Git Bash). La primera ejecución tarda varios minutos y necesita red.

---

## 4. Las capas del proyecto

Aquí la organización es **por capa**: `domain/`, `application/` e `infrastructure/`. Para encontrar todo lo de HU-04 se busca por el nombre `Correspondencia`.

### 4.1 Los papeles que se repiten

| Papel | Dónde | Cómo se reconoce | En HU-04 |
|---|---|---|---|
| Entidad | `domain/` | sin sufijo, con `@Entity` | `Correspondencia` |
| DTO | `application/dto/` | `...DTO`, un `record` | `RegistrarCorrespondenciaDTO` |
| Servicio | `application/service/` | `...Service` | `CorrespondenciaService` |
| Controlador | `infrastructure/controller/` | `...Controller`, `@RestController` | `CorrespondenciaController` |
| Repositorio | `infrastructure/repository/` | `...Repository`, interfaz sin código | `CorrespondenciaRepository` |

Un **DTO** (*Data Transfer Object*) es la forma del cuerpo que llega por HTTP, separada de la entidad atada a la base de datos.

### 4.2 Los archivos de HU-04 — 6 propios y 3 reutilizados

| Archivo | Qué hace |
|---|---|
| `domain/Correspondencia.java` | la entidad: descripción, fecha de recepción, estado, nombre y cédula del destinatario. **Contiene la regla de las transiciones** (`notificar()`, `entregar()`) |
| `domain/EstadoPaquete.java` | el enum `RECIBIDO`, `NOTIFICADO`, `ENTREGADO` |
| `application/dto/RegistrarCorrespondenciaDTO.java` | `record` con `descripcion` y `cedula_usuarios` |
| `application/service/CorrespondenciaService.java` | `registrar`, `notificar`, `entregar` |
| `infrastructure/controller/CorrespondenciaController.java` | las rutas bajo `/api/correspondencias` |
| `infrastructure/repository/CorrespondenciaRepository.java` | acceso a la tabla `correspondencias` |
| *(reutilizado)* `domain/Usuario.java`, `domain/Rol.java`, `infrastructure/repository/UsuarioRepository.java` | el destinatario es un usuario registrado |

Las rutas:

| Ruta | Qué hace | Responde |
|---|---|---|
| `POST /api/correspondencias` | registra (nace `RECIBIDO`) | 201 · 400 |
| `PATCH /api/correspondencias/{id}/notificar` | `RECIBIDO → NOTIFICADO` | 200 · 404 · 409 |
| `PATCH /api/correspondencias/{id}/entregar` | `NOTIFICADO → ENTREGADO` | 200 · 404 · 409 |

Hoy **no hay rutas de consulta** (`GET`): para ver lo guardado se usa la consola H2 o PostgreSQL.

### 4.3 Cómo se convierten los errores en códigos HTTP
Este proyecto no tiene una carpeta de excepciones propias. El servicio lanza dos tipos estándar de Java y el controlador los traduce:

| El servicio lanza | El controlador responde | Cuándo |
|---|---|---|
| `IllegalArgumentException` | 400 en `registrar` · 404 en `notificar`/`entregar` | datos inválidos · paquete inexistente |
| `IllegalStateException` | 409 | transición de estado inválida |

Ojo: un mismo tipo (`IllegalArgumentException`) significa cosas distintas según la ruta. Está registrado como deuda técnica (`docs/deuda-tecnica.md`, punto 9).

### 4.4 Un detalle de diseño
La entidad guarda el estado como texto (`String`), no como enum de Java; `EstadoPaquete` aporta los nombres válidos. En PostgreSQL la columna es un `ENUM`; por eso la URL de conexión lleva `?stringtype=unspecified`.

---

## 5. El recorrido de una petición

Ejemplo: `PATCH /api/correspondencias/{id}/notificar`.

```
HTTP        Controlador        Servicio                Entidad / Repositorio        BD
 │ PATCH .../notificar
 │────────────▶│ notificar(id)
 │             │────────────────▶│ buscar(id) ─────────────▶│ findById ───────────▶│ SELECT
 │             │                 │◀─────────────────────────│◀─────────────────────│
 │             │                 │ correspondencia.notificar()   ← LA REGLA vive aquí:
 │             │                 │    ¿estado == RECIBIDO?  si no → IllegalStateException
 │             │                 │ save ────────────────────▶│ save ───────────────▶│ UPDATE
 │◀────────────│◀────────────────│
 │ 200 + JSON   o   404 (no existe)   o   409 (transición inválida)
```

**El controlador traduce, el servicio coordina, la entidad conoce sus reglas, el repositorio no piensa.** Para buscar una regla de HU-04, empieza por `CorrespondenciaService` y baja a `Correspondencia`.

---

## 6. Las anotaciones que vas a ver

Una anotación es una marca que no hace nada por sí sola: Spring la lee al arrancar y actúa en consecuencia.

| Anotación | Dónde | Qué provoca | En HU-04 |
|---|---|---|---|
| `@SpringBootApplication` | clase de arranque | busca componentes, configura todo y levanta el servidor | `SoftwareApplication` |
| `@Entity` · `@Table(name = "correspondencias")` | una clase | esta clase es una tabla | `Correspondencia` |
| `@Id` · `@GeneratedValue(strategy = UUID)` | un campo | llave primaria; se genera sola | `id` |
| `@Column(name = ..., nullable = false)` | un campo | nombre de columna y si acepta nulos | `fecha_recepcion`, `estado` |
| `@Column(columnDefinition = "TEXT")` | un campo | tipo SQL exacto | `descripcion` |
| `@Service` | una clase | Spring crea una instancia y la inyecta donde se pida | `CorrespondenciaService` |
| `@Repository` | una interfaz | acceso a datos; Spring escribe la implementación | `CorrespondenciaRepository` |
| `@RestController` · `@RequestMapping` | una clase | sus métodos responden HTTP con JSON, bajo una ruta común | `/api/correspondencias` |
| `@PostMapping` · `@PatchMapping` | un método | el verbo HTTP y la ruta | `registrar`, `notificar`, `entregar` |
| `@RequestBody` · `@PathVariable` | un parámetro | el valor sale del cuerpo JSON / de la ruta | el DTO, el `{id}` |
| `@JsonProperty("cedula_usuarios")` | un campo del DTO | el nombre que lleva en el JSON | `cedulaUsuarios` |

**Inyección de dependencias en una frase:** las clases no crean sus colaboradores con `new`; los piden en el constructor y Spring los entrega. Por eso `CorrespondenciaService` se puede probar con repositorios falsos.

---

## 7. La documentación del proyecto

| Archivo | Qué contiene |
|---|---|
| `README.md` | cómo levantar el proyecto y la sección de HU-04 con su trazabilidad criterio → prueba |
| `docs/historias-usuario.md` | las historias y sus criterios de aceptación |
| `docs/evidencia-hu04.txt` | la salida de `./mvnw test` |
| `docs/evidencia-hu04-demo.txt` | la salida de `scripts/evidencia-hu04.sh` en tu equipo (la generas tú) |
| `docs/deuda-tecnica.md` | lo que sabemos que está mal o incompleto en HU-04 |
| `docs/guia-tecnica-hu04.md` | este documento |
| `docs/vision-producto.md`, `problema-duro.md`, `models/` | el contexto del producto y los modelos |
| `docs/uso-ia.md` | la política de uso de IA y la bitácora |

---

## 8. Cómo ejecutar la aplicación

### 8.1 Compilar y probar
```bash
cd backend/walkin-skeleton/software
./mvnw verify
```
Hace, en orden: descarga dependencias → compila → corre las **36 pruebas** → empaqueta el `.jar`. Si una prueba falla, se detiene ahí. Las pruebas usan H2 en memoria: **no necesitan Docker**.

### 8.2 Arrancar con H2 — sin Docker (el camino más corto)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
Queda en `http://localhost:8080`. Hibernate crea las tablas desde las entidades, `data-h2.sql` carga tres usuarios y **todo se borra al apagar**.

La consola web está en `http://localhost:8080/h2-console`:

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:housing;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | *(vacío)* |

Ya conectado:
```sql
SELECT * FROM CORRESPONDENCIAS;
SELECT cedula, nombre FROM USUARIOS;
```
Usuarios disponibles para probar: `1010000002` y `1010000003` (residentes) y `1010000008` (guarda).

### 8.3 Arrancar con PostgreSQL
```bash
docker-compose up -d          # desde la raíz del repositorio
cd backend/walkin-skeleton/software
./mvnw spring-boot:run        # perfil por defecto
```
Usa `database/v1-initial-schema.sql` y los datos persisten entre reinicios. Con `ddl-auto=validate`, Hibernate solo comprueba que las entidades coincidan con el esquema.

> Esta ruta aún no se ha ejecutado en el equipo del autor (sin virtualización); ver `docs/deuda-tecnica.md`, punto 1.

### 8.4 Probar que responde
En Windows usa **Git Bash**, no PowerShell.

```bash
# 1. registrar (nace RECIBIDO). Guardamos el id que devuelve.
ID=$(curl -s -X POST localhost:8080/api/correspondencias -H 'Content-Type: application/json' \
  -d '{"descripcion":"paquete mediano","cedula_usuarios":"1010000002"}' | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo $ID

# 2. notificar   (RECIBIDO -> NOTIFICADO)
curl -s -X PATCH localhost:8080/api/correspondencias/$ID/notificar

# 3. entregar    (NOTIFICADO -> ENTREGADO)
curl -s -X PATCH localhost:8080/api/correspondencias/$ID/entregar

# 4. los errores que prometen los criterios (muestran solo el código HTTP)
curl -s -o /dev/null -w "%{http_code}\n" -X PATCH localhost:8080/api/correspondencias/$ID/entregar   # 409: ya entregado
curl -s -o /dev/null -w "%{http_code}\n" -X PATCH localhost:8080/api/correspondencias/00000000-0000-0000-0000-000000000000/notificar   # 404
curl -s -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/correspondencias \
  -H 'Content-Type: application/json' -d '{"descripcion":"","cedula_usuarios":"1010000002"}'   # 400
```

La respuesta del paso 1 tiene esta forma (el id y la fecha cambian):
```json
{"id":"3f1c...","descripcion":"paquete mediano","fechaRecepcion":"2026-10-09T10:15:30.123","estado":"RECIBIDO","nombreDestinatario":"María Fernanda López","cedulaUsuarios":"1010000002"}
```
Después, en `h2-console`, `SELECT * FROM CORRESPONDENCIAS;` muestra la fila con su estado final.

**Evidencia en un solo comando** (corre `verify`, arranca la app, hace todo lo anterior, verifica cada código HTTP y lo guarda en `docs/evidencia-hu04-demo.txt`):
```bash
bash scripts/evidencia-hu04.sh --mantener
```
Con `--mantener` la aplicación queda corriendo para que abras la consola H2, ejecutes el `SELECT` y tomes la captura. Ciérrala con Ctrl+C.

---

## 9. Las pruebas

### 9.1 Dónde viven
Espejan el código: la prueba de `...application.service.CorrespondenciaService` está en `src/test/java/.../application/service/CorrespondenciaServiceTest.java`.

| Archivo de prueba | Qué prueba | Cuántas |
|---|---|---|
| `CorrespondenciaServiceTest` | `registrar` con repositorios falsos | 4 |
| `CorrespondenciaEstadoServiceTest` | `notificar` y `entregar` con repositorios falsos | 5 |
| `CorrespondenciaControllerTest` | códigos HTTP de `registrar`, con el servicio falso | 2 |
| `CorrespondenciaEstadoControllerTest` | códigos HTTP de `notificar` y `entregar`, con el servicio falso | 6 |
| `CorrespondenciaIntegrationTest` | `registrar` por HTTP con base H2 real | 3 |
| `CorrespondenciaEstadoIntegrationTest` | el flujo completo por HTTP con base H2 real | 4 |
| **Total HU-04** | | **24** |
| Otras historias (login, visitantes, PQRS, arranque) | | 12 |
| **Total del proyecto** | | **36** |

### 9.2 Cómo se corren

| Dónde | Cómo |
|---|---|
| Terminal, todas | `./mvnw test` |
| Terminal, solo HU-04 | `./mvnw test -Dtest='Correspondencia*Test'` |
| Terminal, una clase | `./mvnw test -Dtest=CorrespondenciaServiceTest` |
| Eclipse, todas | clic derecho en la carpeta `src/test/java` → *Run As > JUnit Test* |
| Eclipse, una clase | clic derecho en la clase → *Run As > JUnit Test* (muestra solo las de esa clase) |

Las pruebas de integración tardan unos 10 segundos porque arrancan la aplicación completa con H2.

### 9.3 La anatomía
Toda prueba tiene tres partes en el mismo orden. Un ejemplo real de `CorrespondenciaEstadoServiceTest` (los comentarios son de esta guía):

```java
@Test
void notificaPaqueteRecibidoYPasaANotificado() {
    // PREPARAR: un paquete RECIBIDO y un repositorio falso que lo devuelve
    UUID id = UUID.randomUUID();
    when(correspondenciaRepository.findById(id)).thenReturn(Optional.of(paqueteEn(EstadoPaquete.RECIBIDO)));
    when(correspondenciaRepository.save(any(Correspondencia.class))).thenAnswer(i -> i.getArgument(0));

    // EJECUTAR: una sola llamada
    Correspondencia resultado = service.notificar(id);

    // VERIFICAR
    assertEquals("NOTIFICADO", resultado.getEstado());
}
```
Preparar, ejecutar, verificar. Cuando abras una prueba que no entiendas, busca esas tres partes primero. En este proyecto el nombre del método es la promesa: se lee como una frase.

### 9.4 Las anotaciones de prueba

| Anotación | Qué hace |
|---|---|
| `@Test` | este método es una prueba |
| `@ExtendWith(MockitoExtension.class)` | activa Mockito en esta clase |
| `@Mock` | crea un objeto falso de ese tipo |
| `@InjectMocks` | crea la clase bajo prueba y le inyecta los `@Mock` |
| `@SpringBootTest` | arranca la aplicación completa con H2 |
| `@Transactional` | al terminar cada prueba deshace lo que escribió en la base, así las pruebas no se ensucian entre sí |
| `@BeforeEach` | se ejecuta antes de **cada** prueba (aquí crea el usuario de prueba) |
| `MockMvcBuilders.webAppContextSetup(...)` | llama a la API como si fuera HTTP, sin abrir un puerto |

### 9.5 Qué es un mock, en serio
```java
when(usuarioRepository.findById("123")).thenReturn(Optional.of(residente("123", "Ana Gómez")));
```
Se lee: «cuando alguien le pida al repositorio el usuario 123, devuelve este». El repositorio falso no consulta ninguna base de datos: devuelve lo que tú le dijiste.

> **Un mock solo sabe lo que le enseñaste.** Si no le enseñaste nada, devuelve "vacío".

Eso tuvo una consecuencia real en este proyecto. La prueba `rechazaSiFaltaLaDescripcion` pasaba aunque se borrara la validación de la descripción: sin ella, el repositorio falso devolvía vacío para el usuario, el servicio lanzaba *otro* `IllegalArgumentException` («el destinatario no existe») y `assertThrows` quedaba satisfecho. Ahora la prueba comprueba **el mensaje del error** y que nunca se consultó al usuario, y hay una prueba gemela para el destinatario vacío.

Y el límite de los mocks: no saben nada de la base real ni de los códigos HTTP. Por eso existen las pruebas de integración.

### 9.6 Si rompes la regla, ¿qué prueba falla?
Cada defecto se introdujo en una copia del servicio o de la entidad y se corrieron las 9 pruebas unitarias de servicio. Resultado medido:

| Si rompes... | Prueba que se pone en rojo |
|---|---|
| `entregar` acepta también `RECIBIDO` | `rechazaEntregarSiElPaqueteNoFueNotificado` |
| `notificar` pierde su condición de estado | `rechazaNotificarSiElPaqueteYaFueNotificado` |
| El estado inicial deja de ser `RECIBIDO` | `registraPaqueteConEstadoRecibidoYFechaDeRecepcion` |
| Se quita la validación de la descripción | `rechazaSiFaltaLaDescripcion` |
| Se quita la validación del destinatario vacío | `rechazaSiFaltaElDestinatario` |
| Un destinatario inexistente se acepta | `rechazaSiElDestinatarioNoExiste` |
| `notificar` deja de llamar a `save` | `notificaPaqueteRecibidoYPasaANotificado` |
| `entregar` deja de llamar a `save` | `entregaPaqueteNotificadoYPasaAEntregado` |
| Un paquete inexistente ya no falla | `rechazaCambiarEstadoSiElPaqueteNoExiste` |

Para repetirlo tú: haz el cambio, corre `./mvnw test -Dtest='Correspondencia*Test'`, guarda la captura del rojo y revierte con `git checkout .`.

---

## 10. Comandos que vas a usar

| Comando | Qué hace |
|---|---|
| `./mvnw test` | compila y corre las pruebas |
| `./mvnw test -Dtest='Correspondencia*Test'` | corre solo las de HU-04 |
| `./mvnw verify` | compila, prueba y empaqueta |
| `./mvnw clean` | borra `target/`. Úsalo cuando algo huela raro |
| `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2` | arranca la aplicación sin Docker |
| `./mvnw dependency:tree` | muestra todas las librerías que entraron y por qué |
| `bash scripts/evidencia-hu04.sh` | deja la evidencia de HU-04 en `docs/evidencia-hu04-demo.txt` |
| `docker-compose up -d` | levanta PostgreSQL |
| `git log --oneline --graph` | la historia del proyecto en una pantalla |
| `git status` | qué has cambiado sin confirmar |

---

## 11. Errores comunes al arrancar

| Síntoma | Causa casi siempre | Salida |
|---|---|---|
| `invalid target release: 21` | tu JDK es anterior a 21 | instala un JDK 21 y regístralo en el IDE |
| `Could not resolve dependencies` | sin red en la primera ejecución | conéctate y repite; después queda en caché |
| `mvnw: No such file or directory` | estás en la raíz del repositorio | `cd backend/walkin-skeleton/software` |
| `Connection to localhost:5432 refused` | arrancaste sin el perfil `h2` y no hay PostgreSQL | usa `-Dspring-boot.run.profiles=h2`, o levanta Docker |
| `Port 8080 already in use` | tienes otra aplicación corriendo (a veces la de una ejecución anterior) | apágala, o usa `--server.port=8081` |
| `/h2-console` da 404 | falta el módulo `spring-boot-h2console` (Spring Boot 4) o no usaste el perfil `h2` | revisa el `pom.xml` y el comando de la sección 8.2 |
| h2-console: *Table "CORRESPONDENCIAS" not found* | la JDBC URL no es la de 8.2, o la app ya se cerró | copia la URL tal cual y deja la app corriendo |
| `POST` responde 400 «El destinatario no existe» | la cédula no está en la base | usa `1010000002` con el perfil `h2` |
| `curl` se comporta raro en Windows | PowerShell tiene su propio `curl` | usa Git Bash |
| Eclipse marca errores pero Maven compila | el IDE no ha bajado las dependencias | clic derecho → *Maven > Update Project* |
| Las pruebas pasan en el IDE y fallan en `./mvnw` | el IDE compila distinto | manda `./mvnw`: es la referencia |

---

## 12. Tu primera media hora

1. `cd backend/walkin-skeleton/software` y `./mvnw verify` → **36 pruebas en verde**. Si no, pide ayuda antes de seguir.
2. `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2` y las peticiones de la sección 8.4. Abre `h2-console` y ejecuta `SELECT * FROM CORRESPONDENCIAS;`.
3. Abre `CorrespondenciaService.java` y sigue con el dedo las capas de la sección 5; baja hasta `Correspondencia.notificar()`.
4. Abre `CorrespondenciaEstadoServiceTest.java` y ubica preparar / ejecutar / verificar en una prueba.
5. Lee `docs/deuda-tecnica.md`: es la lista honesta de lo que falta.
6. Rompe una regla a propósito (sección 9.6), corre las pruebas y mira cuál se pone en rojo. Revierte con `git checkout .`.

Con eso ya puedes defender HU-04.
