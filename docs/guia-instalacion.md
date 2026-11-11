# Guía de instalación — Housing Control

Esta guía permite ejecutar el proyecto desde cero en un equipo limpio. Hay dos caminos: con **Docker y PostgreSQL**, o **sin Docker** con una base H2 en memoria (perfil `h2`).

## 1. Requisitos previos

| Requisito | Versión | Para qué |
|---|---|---|
| Git | Cualquiera reciente | Clonar el repositorio |
| JDK | **21** | Compilar y ejecutar el backend |
| Docker Desktop | Reciente | Solo para el camino con PostgreSQL |

Maven no hace falta: el repositorio incluye Maven Wrapper (`mvnw` y `mvnw.cmd`).

**Comprobar Java:**

```bash
java -version
```

Debe mostrar la versión 21. Si muestra otra, instala el [JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) y configura `JAVA_HOME` (sección 5).

## 2. Obtener el código

```bash
git clone https://github.com/lcardona3815-ship-it/houstingcontrol-software.git
cd houstingcontrol-software
git checkout feature/esqueleto-inicial
```

El proyecto vigente está en `backend/software`.

## 3. Camino A — Sin Docker (perfil `h2`)

Es el camino más rápido y no requiere virtualización. La base de datos vive en memoria y se borra al apagar la aplicación.

```bash
cd backend/software
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

En PowerShell:

```powershell
cd backend\software
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

La aplicación queda en http://localhost:8080 con tres usuarios de demostración.

Consola de la base de datos: http://localhost:8080/h2-console

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:housing;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` |
| Usuario | `sa` |
| Contraseña | (vacía) |

## 4. Camino B — Con Docker y PostgreSQL

1. Abre Docker Desktop y espera a que esté en ejecución.
2. Desde la raíz del repositorio, levanta la base de datos:
   ```bash
   docker-compose up -d
   ```
3. Comprueba que el contenedor está activo:
   ```bash
   docker ps
   ```
4. Inicia el backend:
   ```bash
   cd backend/software
   ./mvnw spring-boot:run
   ```
   En PowerShell: `.\mvnw.cmd spring-boot:run`
5. La aplicación queda en http://localhost:8080.

Los scripts de la base de datos están en `database/`:

- `v1-initial-schema.sql`: esquema (tablas y restricciones).
- `insercion-datos.sql`: datos de prueba. Para probar PQRS necesitas al menos un usuario registrado; si tu entorno no los cargó automáticamente al levantar el contenedor, ejecútalos manualmente contra la base.

Para detener y limpiar:

```bash
docker-compose down
```

## 5. Configuración

| Elemento | Dónde se configura |
|---|---|
| Conexión a PostgreSQL (URL, usuario, contraseña) | `backend/software/src/main/resources/application.properties` |
| Usuario y contraseña del contenedor | `docker-compose.yml` (deben coincidir con `application.properties`) |
| Perfil H2 | `backend/software/src/main/resources/application-h2.properties` |
| Datos de demostración de H2 | `backend/software/src/main/resources/data-h2.sql` |
| Configuración de pruebas | `backend/software/src/test/resources/application.properties` |
| Puerto del servidor | 8080 por defecto |

**Variable de entorno `JAVA_HOME`** (si `java -version` no muestra la versión 21):

- Windows (PowerShell): `setx JAVA_HOME "C:\Program Files\Java\jdk-21"` y reabrir la terminal.
- Linux / macOS: `export JAVA_HOME=/ruta/al/jdk-21`

No subas contraseñas reales al repositorio; las de `docker-compose.yml` y `application.properties` son solo para desarrollo.

## 6. Verificar la instalación

1. **Arranque correcto:** en la terminal debe aparecer un mensaje como `Started SoftwareApplication` y la aplicación no debe cerrarse.
2. **Pruebas automáticas:**
   ```bash
   cd backend/software
   ./mvnw test
   ```
   Resultado esperado a fecha de esta guía: `Tests run: 52, Failures: 0, Errors: 0` y `BUILD SUCCESS`. Las pruebas usan H2 en memoria, así que no necesitan Docker.
3. **Consola H2** (camino A): http://localhost:8080/h2-console debe abrir e iniciar sesión con los datos de la sección 3.

## 7. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `cd: housingcontrol-software: No such file` | Nombre de carpeta mal escrito | El repositorio se clona como `houstingcontrol-software` (con "t") |
| `java: command not found` o versión distinta de 21 | JDK no instalado o `JAVA_HOME` incorrecto | Instalar JDK 21 y revisar `JAVA_HOME` |
| `Permission denied` al ejecutar `./mvnw` (Linux/macOS) | El wrapper no tiene permiso de ejecución | `chmod +x mvnw` |
| `Port 8080 was already in use` | Otra aplicación usa el puerto | Cerrar esa aplicación o ejecutar con `--server.port=8081` |
| Docker no inicia o pide virtualización | Virtualización deshabilitada en BIOS/Windows | Usar el camino A (perfil `h2`) |
| `Connection refused` a PostgreSQL | Contenedor apagado o puerto ocupado | `docker ps`; reiniciar con `docker-compose up -d` |
| Fallo de autenticación con PostgreSQL | Credenciales distintas entre `docker-compose.yml` y `application.properties` | Hacer que coincidan y recrear el contenedor |
| Las pruebas descargan mucho la primera vez | Maven baja dependencias | Esperar; las siguientes ejecuciones son rápidas |
| Una petición responde `401` o `403` | La API tiene Spring Security activo | Consultar `docs/seguridad-y-rnf.md` |

## 8. Documentación relacionada

- [README](../README.md)
- [Guía técnica de HU-04](guia-tecnica-hu04.md)
- [Informe técnico](informe-tecnico.md)
- [Seguridad y requisitos no funcionales](seguridad-y-rnf.md)
