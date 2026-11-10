# TP1 · Spring Boot, API REST y arquitectura en capas

Punto de partida del práctico. Está armada la **configuración e
infraestructura transversal** que van a necesitar sin importar cómo
resuelvan cada consigna; lo que falta —el diseño y la lógica propia de cada
recurso— se va a ir sumando a esta rama a medida que avance la cursada.

## Cómo levantar el proyecto

Requiere Java 25. Usar siempre el wrapper, nunca un `mvn` instalado aparte:

```
# Windows
.\mvnw.cmd spring-boot:run

# macOS/Linux
./mvnw spring-boot:run
```

Cuando el log muestre `Started DemoApplication`, la app queda escuchando en
`http://localhost:8080`.

Para compilar y correr los tests: `./mvnw test` (o `.\mvnw.cmd test`).

## Endpoints disponibles hoy

| Método | Path | Qué hace |
|---|---|---|
| GET | `/health` | Chequeo de salud básico |
| GET | `/ping` | Devuelve `pong`, sin JSON — otro chequeo trivial |

```
curl http://localhost:8080/health
curl http://localhost:8080/ping
```

## Qué ya está armado

- **`config/RestClientConfig`**: bean de `RestClient` apuntado a la
  `base-url` de DummyJSON (`app.dummyjson.base-url` en
  `application.properties`). Listo para inyectar.
- **`config/OpenApiConfig`**: metadata general de Swagger UI.
- **`client/dummyjson/DummyJsonProducto` y `DummyJsonProductosResponse`**:
  la forma exacta del JSON que devuelve `https://dummyjson.com/products` —
  para no tener que adivinar los nombres de campo del proveedor externo.
- **`exception/GlobalExceptionHandler`** (+ `RecursoNoEncontradoException` y
  `ServicioExternoException`): manejo uniforme de errores para toda la API
  (`ProblemDetail`). Ya contempla 404 y errores de un servicio externo —
  se reusa tal cual para cualquier recurso nuevo que se agregue.

## Qué falta (eso es la consigna)

- Un cliente propio (`DummyJsonClient` o como se llame) que use el
  `RestClient` ya configurado para llamar a `/products` y `/products/{id}`,
  manejando los errores de red/HTTP con las excepciones ya definidas.
- Un DTO propio para el producto (no el JSON externo tal cual) y el
  service/controller de `/api/productos`.
- Todo el recurso de favoritos: entidad, repository en memoria, DTOs,
  service y controller CRUD.
- Anotar los controllers con `@Tag`/`@Operation` para que Swagger UI los
  documente.

## Dependencias

- `spring-boot-starter-webmvc` — Spring MVC + Tomcat embebido.
- `spring-boot-starter-validation` — Bean Validation (`@NotNull`, `@NotBlank`, ...).
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI / OpenAPI.


## Instrucciones para levantar el proyecto

1. Abrir una terminal en la carpeta raíz del proyecto.
2. Ejecutar el Maven Wrapper para iniciar la aplicación:
   - **En Windows:** `mvnw.cmd spring-boot:run`
   - **En Linux / Mac:** `./mvnw spring-boot:run`
3. La aplicación se iniciará en el puerto `8080`.

## Documentación de la API
Una vez levantado el proyecto, podés explorar y probar todos los endpoints a través de Swagger UI ingresando a:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


## Levantar la Base de Datos y Migraciones

Este proyecto utiliza PostgreSQL como base de datos relacional y Flyway para el versionado del esquema.

1. **Levantar PostgreSQL:** 
   Posicionate en la raíz del proyecto y ejecutá:
   `docker compose up -d`
   Esto descargará la imagen oficial y levantará un contenedor en el puerto 5432.

2. **Migraciones Automáticas:**
   No es necesario ejecutar scripts SQL manualmente. Al arrancar la aplicación de Spring Boot (`./mvnw spring-boot:run`), Flyway detectará automáticamente los archivos en `src/main/resources/db/migration` y aplicará los cambios pendientes en el esquema antes de que la aplicación empiece a aceptar peticiones.

   ## 4. Puertos y adaptadores: qué cambió y qué no

### Comparación antes y después de la migración a JPA
Al migrar el almacenamiento de nuestros favoritos desde una lista en memoria hacia una base de datos PostgreSQL usando JPA, la estructura de nuestra aplicación demostró en la práctica los beneficios de la Arquitectura Hexagonal.

**Clases que quedaron EXACTAMENTE IGUALES (Intactas):**
* `FavoritoService`: La lógica de negocio no sufrió ninguna modificación.
* `FavoritoController`: Los endpoints y la capa de presentación web siguen siendo los mismos.
* `Favorito` (Modelo): El objeto puro del dominio se mantuvo inalterado.
* `FavoritoDto`: Los objetos de transferencia de datos hacia el exterior no cambiaron.
* `FavoritoRepository` (Interfaz): El contrato de operaciones se mantuvo idéntico.

**Clases que se AGREGARON, MODIFICARON o ELIMINARON (Infraestructura):**
* **Eliminada:** `InMemoryFavoritoRepository` (el adaptador viejo).
* **Agregadas:** `FavoritoEntity` (modelo de base de datos), `FavoritoJpaRepository` (interfaz de Spring Data) y `FavoritoRepositoryAdapter` (el nuevo adaptador).

### ¿Por qué fue posible este cambio?
Esto fue posible porque aplicamos el patrón de **Puertos y Adaptadores**. 

En nuestro diseño, la interfaz `FavoritoRepository` actúa como un **puerto** (un contrato estricto). Nuestro `FavoritoService` (el núcleo de la aplicación) solo conoce este puerto y le es totalmente indiferente cómo se guardan los datos por detrás. 

Tanto la clase `InMemoryFavoritoRepository` del TP1 como la nueva `FavoritoRepositoryAdapter` del TP2 son simplemente **adaptadores intercambiables** que implementan esa misma interfaz. Al "desenchufar" el adaptador en memoria y "enchufar" el de JPA, el núcleo de la aplicación ni se enteró del cambio de tecnología. Esto nos permitió migrar toda la base de datos sin tocar una sola línea de código de nuestra lógica de negocio o nuestros controladores.

## 6. Evolución del esquema
Las modificaciones a la base de datos se resuelven agregando una nueva migración (V4) en lugar de modificar las anteriores (V1, V2 o V3) por cómo funciona la herramienta de control de versiones (Flyway). 

Flyway registra en la base de datos qué scripts ya fueron ejecutados y calcula un *checksum* (un hash) de sus contenidos. Si modificamos un archivo del pasado, su *checksum* cambia, Flyway detecta la inconsistencia y aborta el arranque de la aplicación para proteger la integridad del entorno. La evolución de esquemas relacionales siempre debe hacerse "hacia adelante" (roll-forward) mediante nuevos archivos inmutables.

## 7. Transacciones y propiedades ACID
El método `moverFavoritos` fue anotado con `@Transactional` para garantizar la **Atomicidad** (la 'A' en ACID). 

Esta operación consta de dos escrituras clave en la base de datos: actualizar el `lista_id` de varios favoritos y eliminar la lista de origen. Si no usáramos `@Transactional` y el sistema fallara justo después de mover los favoritos pero antes de eliminar la lista, la base de datos quedaría en un **estado inconsistente**: los favoritos ya estarían en el nuevo destino, pero la lista de origen seguiría existiendo de forma residual y vacía, rompiendo la lógica de nuestra operación que exigía su eliminación. 

Gracias a `@Transactional`, Spring envuelve ambas operaciones en un único bloque. Si la eliminación final falla, se ejecuta un *rollback* automático y todos los favoritos vuelven a su lista original, asegurando que la operación se aplique completa o no se aplique en absoluto.

## Configuración de PostgreSQL y Migraciones

Para ejecutar esta aplicación, se requiere una instancia de PostgreSQL versión 16+.

**Opción A: Levantar con Docker (Recomendado)**
Puedes iniciar rápidamente un contenedor efímero con el siguiente comando:
```bash
docker run --name postgres-tp2 -e POSTGRES_USER=tu_usuario -e POSTGRES_PASSWORD=tu_password -e POSTGRES_DB=tp2_db -p 5432:5432 -d postgres:16.15
```

## 8. Autorización y Propiedad de Recursos (Seguridad)

### Dónde vive la regla de autorización
Las reglas de autorización, pertenencia y validación de rol (`ADMIN` vs `USER`) viven en la **capa de servicio (`ListaService`)**.

### ¿Por qué se eligió implementarla en el Service y no en el Controller?

1. **Separación clara de responsabilidades (SoC):**
   Los controladores (`@RestController`) tienen como única responsabilidad mediar entre el protocolo HTTP y la aplicación: deserializar el cuerpo del request, extraer parámetros de ruta o consulta, y mapear el resultado a códigos de estado HTTP. La regla que dictamina quién es dueño de un recurso y si tiene permiso para operar sobre él es **lógica de negocio fundamental**, y por lo tanto pertenece a la capa de servicio.

2. **Seguridad robusta y prevención de bypass:**
   Si la autorización se implementara en el Controller, cualquier otro punto de entrada presente o futuro (como jobs programados, consumidores de colas/eventos, tareas batch, o llamados internos entre servicios) tendría que acordarse de reimplementar la validación manualmente. Al ubicarse en `ListaService`, la regla está centralizada y garantizada sin importar cómo o desde dónde se invoque.

3. **Inmutabilidad de la propiedad al crear recursos:**
   Al crear una lista, el dueño se asigna directamente desde el `UsuarioPrincipal` del `SecurityContextHolder` dentro de `ListaService.crear(lista)`. Esto asegura que el cliente jamás pueda falsear o spoofear el `usuarioId` enviando un ID ajeno en el cuerpo JSON del request.

4. **Distinción semántica entre 404 Not Found y 403 Forbidden:**
   En el servicio se valida primero la existencia del recurso mediante `listaRepository.findById(id)`. Si el recurso no existe, se arroja `RecursoNoEncontradoException` (HTTP 404). Si el recurso existe pero el usuario autenticado no es el dueño ni posee rol `ADMIN`, se arroja `AccesoDenegadoException` (HTTP 403 Forbidden). Esto evita confundir al cliente entre "el recurso no existe" y "el recurso existe pero no tienes permiso para accederlo".

5. **Testeabilidad:**
   Permite escribir pruebas unitarias directas, limpias y muy rápidas sobre `ListaService` para simular diferentes usuarios y roles sin requerir el despliegue de infraestructura web ni MockMvc.

## 9. Suite de Tests y Ejecución

La arquitectura de pruebas está dividida de forma estricta entre pruebas unitarias e integración:

- **Pruebas Unitarias (`*Test`):**
  - Ejecutadas por **Maven Surefire Plugin**.
  - Aisladas y sin dependencias externas (usan mocks con Mockito).
  - Verifican la lógica pura de negocio: `UsuarioServiceImplTest` valida que las contraseñas nunca se persistan en texto plano y que se asigne el rol `USER`; `ListaServiceImplTest` valida la asignación automática del usuario autenticado y la protección de listas ajenas.
  - Comando:
    ```bash
    ./mvnw test
    ```

- **Pruebas de Integración (`*IT`):**
  - Ejecutadas por **Maven Failsafe Plugin**.
  - Levantan un contenedor real de PostgreSQL 16 mediante **Testcontainers**, aplican todas las migraciones Flyway (`V1` a `V6`) sobre una base limpia y ejecutan solicitudes HTTP reales vía `MockMvc`.
  - Casos cubiertos en `ListaControllerIT`:
    - `login_credencialesValidas_retornaTokenOk` (200 con JWT).
    - `login_credencialesInvalidas_retornaUnauthorized` (401).
    - `obtenerLista_usuarioDuenio_retornaOk` (200).
    - `obtenerLista_usuarioAjeno_retornaForbidden` (403 Forbidden).
    - `listarListas_sinToken_retornaUnauthorized` (401).
    - `obtenerLista_adminListaAjena_retornaOk` (200 con rol ADMIN).
  - Comando:
    ```bash
    ./mvnw verify
    ```
  *(Nota: `./mvnw verify` ejecuta primero los tests unitarios y luego los tests de integración).*

## 10. Integración Continua (CI con GitHub Actions)

El proyecto cuenta con un pipeline de CI automatizado definido en [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

- **Disparadores:** Se ejecuta en cada `push` a cualquier rama y en cada `pull_request`.
- **Entorno:** Corre en una máquina virtual limpia `ubuntu-latest` con JDK 25 (Temurin).
- **Ejecución:** Corre `./mvnw verify --no-transfer-progress`, garantizando la ejecución de Surefire (`*Test`), Failsafe (`*IT`), y Testcontainers (Docker en el runner).
- **Cómo leer el resultado:**
  - En la pestaña **Actions** de GitHub ([https://github.com/FrankSibona/demo/actions](https://github.com/FrankSibona/demo/actions)):
    - **Check verde (Success):** El build compiló, los 37+ tests unitarios y los tests de integración con PostgreSQL pasaron sin fallos.
    - **Cruz roja (Failure):** Algún test unitario o de integración falló, o hubo un error de compilación. Al hacer clic en el run y en el job `Build & Verify`, se despliega el log exacto con la aserción que falló.
  - **Ejecución de referencia exitosa:** [Workflow Run #42872004860](https://github.com/FrankSibona/demo/actions/runs/42872004860).

## 11. Despliegue en la Nube (Render + PostgreSQL)

El despliegue en producción fue implementado sobre la plataforma **Render**, desacoplando toda configuración sensible a través de variables de entorno:

### Arquitectura de Despliegue
- **Contenedor Docker Multi-etapa (`Dockerfile`):**
  - Etapa 1 (`eclipse-temurin:25-jdk-noble`): compila el artefacto JAR empaquetado.
  - Etapa 2 (`eclipse-temurin:25-jre-noble`): runtime ligero ejecutado bajo un usuario sin privilegios de root (`spring:spring`).
- **Base de Datos:** Instancia gestionada de PostgreSQL 16 en Render (`tp-postgres`).
- **Variables de Entorno Configuradas:**
  - `PORT`: Puerto asignado por Render (por defecto mapeado a `8080`).
  - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`: Credenciales de conexión inyectadas a `spring.datasource.url`.
  - `JWT_SECRET`: Clave simétrica de 256 bits para la firma de tokens JWT.
  - `JWT_EXPIRATION`: Tiempo de expiración del token (86400000 ms = 24h).

### Pasos Seguidos para el Despliegue
1. Se definió el archivo de infraestructura como código [`render.yaml`](render.yaml) que automatiza la vinculación entre la base de datos y la API.
2. Se conectó el repositorio `FrankSibona/demo` en [Render Dashboard](https://dashboard.render.com).
3. Se aprovisionó el servicio mediante **Blueprint** o creando el Web Service (Docker) y PostgreSQL.
4. Al arrancar, Flyway ejecutó automáticamente las 6 migraciones (`V1` a `V6`) en la base de datos remota.
5. Se verificó la disponibilidad pública mediante `GET /health` (`{"status":"ok"}`) y el correcto funcionamiento del filtro de seguridad JWT (respondiendo `401 Unauthorized` al consultar `/api/listas` sin token y `200 OK` al enviar `Authorization: Bearer <token>`).

