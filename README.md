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