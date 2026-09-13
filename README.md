# Alquiler de Bicicletas

<img width="300" height="300" alt="image" src="https://github.com/user-attachments/assets/8c882763-0e19-44de-9acb-fc05fb407d7e" />

![CI](https://github.com/Manco312/AlquilerBicicletasCeiba/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=spring)
![License](https://img.shields.io/github/license/Manco312/AlquilerBicicletasCeiba)

API REST para gestionar el alquiler de bicicletas de una empresa de turismo urbano: control de disponibilidad, inicio/fin de alquileres, cálculo automático de costos y multas por devolución tardía.

## Tabla de contenidos

- [Tecnologías y dependencias](#tecnologías-y-dependencias)
- [Cómo ejecutar el proyecto localmente](#cómo-ejecutar-el-proyecto-localmente)
- [Ejecutar las pruebas automatizadas](#ejecutar-las-pruebas-automatizadas)
- [Arquitectura elegida y justificación](#arquitectura-elegida-y-justificación)
- [Supuestos e interpretaciones del enunciado](#supuestos-e-interpretaciones-del-enunciado)
- [Modelo de datos](#modelo-de-datos)
- [Endpoints](#endpoints)
- [Ejemplos de peticiones (curl)](#ejemplos-de-peticiones-curl)
- [Prueba de Despliegue](#prueba-de-despliegue)

## Tecnologías y dependencias

- **Java 17**
- **Spring Boot 4.1.1** (Spring Framework 7 / Jackson 3)
  - `spring-boot-starter-webmvc` — API REST
  - `spring-boot-starter-data-jpa` — persistencia
  - `spring-boot-starter-validation` — Bean Validation (`@Valid`)
  - `spring-boot-h2console` + `com.h2database:h2` — base de datos en memoria
  - `lombok` (uso puntual)
- **Maven** como build tool
- **JUnit 5 + Mockito + Spring Boot Test (MockMvc)** para pruebas

> Nota: este proyecto fue generado con Spring Boot **4.1.1**, una versión reciente que reorganiza algunos starters (p. ej. `spring-boot-starter-webmvc` en vez de `spring-boot-starter-web`, y test-starters separados como `spring-boot-starter-webmvc-test`) y migra a **Jackson 3** (`tools.jackson.*` en lugar de `com.fasterxml.jackson.*`). Si tu entorno de evaluación usa Spring Boot 3.x, la lógica de negocio (capas `calculo`, `service`, `model`) es igualmente válida; solo cambiarían nombres de starters/paquetes de Jackson.

## Cómo ejecutar el proyecto localmente

Requisitos: JDK 17+ y conexión a internet la primera vez (para descargar dependencias de Maven). No se necesita instalar Maven.

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Al arrancar, se cargan automáticamente las 5 bicicletas de referencia del enunciado.

Consola H2 (para inspeccionar el estado de la BD en memoria mientras se desarrolla):
`http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:mem:bicidb`, usuario `sa`, sin contraseña.

### Ejecutar las pruebas automatizadas

```bash
.\mvnw.cmd test      # Windows
./mvnw test          # Linux / macOS
```

22 pruebas: cálculo de tarifas y multas (puras, sin Spring), orquestación del servicio de alquileres (Mockito) y un flujo de integración end-to-end contra H2 real vía MockMvc.

## Arquitectura elegida y justificación

Arquitectura **en capas (layered)**, sin necesidad de frameworks de mensajería, CQRS ni microservicios ya que para el alcance del ejercicio (una API CRUD con reglas de negocio acotadas) esas alternativas serían sobre-ingeniería y dificultarían la evaluación de fundamentos.

<img width="570" height="450" alt="image" src="https://github.com/user-attachments/assets/9fd5c0fd-c3c6-4915-9cde-688dcec923c2" />


```
com.ceiba.bicialquiler/
├── controller/    # Adaptadores HTTP: reciben el request, delegan y traducen a ResponseEntity
├── service/       # Casos de uso e interfaces (contratos); service/impl/ las implementaciones
├── calculo/        # Cálculo puro de tarifas y multas (RN-01 a RN-03), sin dependencias de infraestructura
├── repository/    # Spring Data JPA (acceso a datos)
├── model/         # Entidades JPA con comportamiento (no simples "bolsas de getters/setters")
├── dto/           # Contratos de entrada/salida de la API, desacoplados de las entidades
├── exception/     # Excepciones de negocio + GlobalExceptionHandler (@RestControllerAdvice)
├── enums/         # TipoBicicleta (con su tarifa) y EstadoBicicleta
└── config/        # DataLoader (seed de datos de referencia)
```

Decisiones de diseño destacadas:

1. **`CalculadoraTarifas` aislada del resto del servicio** (paquete `calculo`): recibe tipo de bicicleta, duración real y duración estimada, y devuelve un `ResultadoCalculo` con el desglose completo (horas cobradas, costo base, horas de retraso, multa, total). Es una clase de cálculo puro —sin repositorios, sin JPA, sin HTTP— por lo que las reglas de negocio más importantes (RN-01 a RN-03) se prueban unitariamente sin mocks ni base de datos. Esto es lo que pide explícitamente el enunciado como diferenciador de diseño.
2. **Redondeo al alza sin `Math.ceil`/`double`**: se usa aritmética entera sobre segundos (`(segundos + 3599) / 3600`), evitando errores de precisión de punto flotante y cubriendo el caso borde "2h exactas no redondea a 3h".
3. **Tarifa como atributo del enum** (`TipoBicicleta.getTarifaPorHora()`): evita un `Map` paralelo que se pueda desincronizar del enum. Alternativa válida y documentada: si las tarifas debieran cambiar sin desplegar, se movería a una tabla de configuración.
4. **DTOs (records) en vez de exponer entidades JPA**: la API nunca serializa `Bicicleta`/`Alquiler` directamente, evitando acoplar el contrato HTTP al modelo de persistencia y problemas de serialización con proxies/lazy-loading de Hibernate.
5. **`Alquiler.finalizar(...)` como método de dominio**: la entidad no permite quedar en un estado inconsistente (hora de fin sin costo calculado, o viceversa); la transición de estado vive en el propio modelo, no dispersa en el service.
6. **Bloqueo optimista (`@Version`) en `Bicicleta`**: mitiga la condición de carrera de dos alquileres concurrentes sobre la misma bicicleta.
7. **Manejo de errores centralizado**: excepciones de negocio específicas (`BicicletaNoDisponibleException`, `AlquilerNoEncontradoException`, etc.) mapeadas una sola vez en `GlobalExceptionHandler` a códigos HTTP y mensajes descriptivos, en vez de `try/catch` repetido en cada controller (DRY).
8. **`AlquilerResponse` reutilizado para RF-03 y RF-05**: el detalle de un alquiler y cada fila del historial tienen exactamente el mismo contrato, así que se usa un único DTO en vez de duplicarlo.
9. **Un único origen para los datos de referencia** (`DatosReferencia.bicicletasDeReferencia()`): tanto `DataLoader` (al arrancar la app en desarrollo) como `AlquilerFlowIntegrationTest` (al preparar cada prueba) siembran exactamente las mismas 5 bicicletas del enunciado, en vez de mantener dos listas que se puedan desincronizar.

## Supuestos e interpretaciones del enunciado

- **Hora de inicio/fin configurables por request, con valor por defecto "ahora"**: `IniciarAlquilerRequest.horaInicio` y `FinalizarAlquilerRequest.horaFin` son opcionales. Si no se envían, el servidor usa `LocalDateTime.now()`. Se decidió así (en vez de forzar siempre la hora del servidor) para poder probar de forma determinística las reglas de redondeo/multa (RN-02/RN-03) sin depender de `Thread.sleep` ni de relojes simulados, y para permitir registros administrativos tardíos. En un entorno productivo con clientes no confiables, se recomendaría ignorar `horaInicio`/`horaFin` del cliente y usar siempre el reloj del servidor (o exigir un rol de "administrador" para poder fijarlas).
- **Duración mínima facturable de 1 hora**: un alquiler devuelto en menos de una hora igual cobra 1 hora completa (interpretación razonable de "redondeado al alza", ya que no se especifica un mínimo explícito para el costo base como sí se hace para la multa).
- **Multa mínima de 1 hora ante cualquier retraso** (explícito en RN-03): cualquier retraso mayor a 0 segundos cobra al menos 1 hora de multa; esto surge naturalmente de la misma fórmula de redondeo al alza.
- **`estado` es opcional al crear una bicicleta** (RF-01): si no se informa, se asume `DISPONIBLE`. Esto permite cargar bicicletas ya en mantenimiento o alquiladas (como `BIC-004` en los datos de referencia) sin necesitar un endpoint adicional de cambio de estado.
- **Código de bicicleta único**: se rechaza con `409 Conflict` el registro de una bicicleta con un código ya existente (no estaba explícito, pero se infiere de "código único").
- **RN-05 con dos causas distintas de error**: "no existe" se mapea a `404 Not Found`; "ya fue finalizado" se mapea a `409 Conflict` (existe el recurso, pero la operación no es válida en su estado actual). Ambos casos devuelven un mensaje descriptivo distinto.
- **Seguridad**: no se implementó autenticación/autorización (no estaba en el alcance para un nivel practicante y añadir `spring-boot-starter-security` sin diseñarla bien habría bloqueado todos los endpoints por defecto). Lo que sí se aplicó como seguridad básica: validación estricta de entrada (`@Valid` + Bean Validation) para evitar payloads malformados, uso exclusivo de Spring Data JPA con *derived queries* (parámetros vinculados, sin SQL concatenado, sin superficie de inyección SQL), y mensajes de error que no filtran detalles internos (stack traces, SQL) al cliente. En producción se agregaría autenticación por API key o JWT y HTTPS obligatorio.

## Modelo de datos

- **Bicicleta**: `id`, `codigo` (único), `tipo` (`URBANA` | `MONTAÑA` | `ELÉCTRICA`), `estado` (`DISPONIBLE` | `ALQUILADA` | `EN_MANTENIMIENTO`).
- **Alquiler**: `id`, bicicleta asociada, `clienteNombre`, `horaInicio`, `duracionEstimadaHoras`, `horaFin` (null mientras está activo), `costoBase`, `montoMulta`, `costoTotal`, `tuvoMulta`.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/bicicletas` | Registrar una bicicleta (RF-01) |
| GET | `/api/bicicletas` | Listar todas las bicicletas |
| GET | `/api/bicicletas/disponibles?tipo=` | Bicicletas disponibles, filtro de tipo opcional (RF-04) |
| GET | `/api/bicicletas/{codigo}/historial` | Historial de alquileres de una bicicleta (RF-05) |
| POST | `/api/alquileres` | Iniciar un alquiler (RF-02) |
| PUT | `/api/alquileres/{id}/finalizar` | Finalizar un alquiler y calcular el costo (RF-03) |

## Ejemplos de peticiones (curl)

> En Windows con `cmd.exe`/PowerShell, reemplaza las comillas simples por dobles escapadas si copias tal cual; los ejemplos de abajo funcionan directamente en bash/Git Bash/WSL.

### Registrar una bicicleta

```bash
curl -X POST http://localhost:8080/api/bicicletas \
  -H "Content-Type: application/json" \
  -d '{"codigo":"BIC-010","tipo":"URBANA"}'
```

### Consultar disponibilidad (todas / filtradas por tipo)

```bash
curl http://localhost:8080/api/bicicletas/disponibles

curl "http://localhost:8080/api/bicicletas/disponibles?tipo=MONTA%C3%91A"
```

### Iniciar un alquiler

```bash
curl -X POST http://localhost:8080/api/alquileres \
  -H "Content-Type: application/json" \
  -d '{"codigoBicicleta":"BIC-001","clienteNombre":"Camilo Restrepo","duracionEstimadaHoras":2}'
```

Respuesta (201 Created):

```json
{
  "id": 1,
  "codigoBicicleta": "BIC-001",
  "tipoBicicleta": "URBANA",
  "clienteNombre": "Camilo Restrepo",
  "horaInicio": "2026-09-13T10:00:00",
  "horaFin": null,
  "duracionEstimadaHoras": 2,
  "duracionRealMinutos": null,
  "costoBase": null,
  "montoMulta": null,
  "costoTotal": null,
  "tuvoMulta": false,
  "activo": true
}
```

### Finalizar un alquiler

```bash
curl -X PUT http://localhost:8080/api/alquileres/1/finalizar \
  -H "Content-Type: application/json" \
  -d '{}'
```

Respuesta (200 OK) — ejemplo replicando el caso del enunciado (bicicleta MONTAÑA, estimada 2h, devuelta a las 3h20min):

```json
{
  "id": 2,
  "codigoBicicleta": "BIC-002",
  "tipoBicicleta": "MONTAÑA",
  "clienteNombre": "Ana Gómez",
  "horaInicio": "2026-09-13T10:00:00",
  "horaFin": "2026-09-13T13:20:00",
  "duracionEstimadaHoras": 2,
  "duracionRealMinutos": 200,
  "costoBase": 20000.00,
  "montoMulta": 5000.00,
  "costoTotal": 25000.00,
  "tuvoMulta": true,
  "activo": false
}
```

### Historial de una bicicleta

```bash
curl http://localhost:8080/api/bicicletas/BIC-001/historial
```

### Ejemplos de error

Bicicleta no disponible (RN-04):

```bash
curl -X POST http://localhost:8080/api/alquileres \
  -H "Content-Type: application/json" \
  -d '{"codigoBicicleta":"BIC-004","clienteNombre":"Ana","duracionEstimadaHoras":1}'
```

```json
{
  "timestamp": "2026-09-13T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "La bicicleta 'BIC-004' no está disponible para alquiler (estado actual: EN_MANTENIMIENTO)",
  "path": "/api/alquileres"
}
```

Alquiler inexistente al finalizar (RN-05):

```bash
curl -X PUT http://localhost:8080/api/alquileres/999/finalizar -H "Content-Type: application/json" -d '{}'
```

```json
{
  "timestamp": "2026-09-13T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No existe un alquiler con id 999",
  "path": "/api/alquileres/999/finalizar"
}
```

## Prueba de Despliegue

### Dockerfile

El proyecto incluye un `Dockerfile` multi-stage para poder construir y ejecutar la aplicación en un contenedor sin depender de un JDK ni un Maven instalados en el host:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Etapa `build`**: usa la imagen `eclipse-temurin:17-jdk-alpine` (JDK 17 sobre Alpine, liviana) para compilar el proyecto. Primero copia únicamente el *wrapper* de Maven (`mvnw`, `.mvn/`) y el `pom.xml`, y descarga las dependencias (`dependency:go-offline`) antes de copiar el código fuente. Esto aprovecha la caché de capas de Docker: si no cambian las dependencias, no hace falta volver a descargarlas en cada build. Luego copia `src/` y empaqueta el `.jar` con `./mvnw package -DskipTests` (los tests ya se ejecutan en CI, no es necesario repetirlos al construir la imagen).
- **Etapa final**: parte de `eclipse-temurin:17-jre-alpine`, una imagen mucho más liviana que solo trae el *runtime* (JRE) en lugar del JDK completo, ya que en producción no se necesita compilar nada. Copia únicamente el `.jar` generado en la etapa anterior (`COPY --from=build`), expone el puerto `8080` y define el `ENTRYPOINT` que arranca la aplicación con `java -jar app.jar`.
- **Puerto dinámico**: `application.properties` define `server.port=${PORT:8080}`, es decir, la aplicación toma el puerto de la variable de entorno `PORT` si existe (como la asigna Render dinámicamente) y usa `8080` como valor por defecto para ejecución local. Esto permite usar la misma imagen tanto en local como en un proveedor cloud que inyecta su propio puerto.

### Despliegue en Render

El despliegue se realizó en [Render](https://render.com) como un servicio *Web Service*, usando el `Dockerfile` del repositorio como método de build (Render detecta el `Dockerfile`, construye la imagen y la ejecuta directamente, sin necesidad de configurar un *build command* ni un *start command* manuales).

> ⚠️ **Nota importante**: el servicio está desplegado en el plan gratuito de Render, el cual apaga (duerme) la instancia tras un periodo de inactividad. Por esta razón, **no se garantiza que el despliegue esté disponible al momento de revisar este README**.

### Pruebas realizadas contra el despliegue

Se probaron los endpoints principales de la API ya desplegada en Render usando Postman, apuntando a la URL pública del servicio en lugar de `localhost`.

**1. Iniciar un alquiler (`POST /api/alquileres`)**

<img width="600" height="400" alt="image" src="https://github.com/user-attachments/assets/ecd528d1-3b0a-4d16-a3a9-cd100219b399" />

**2. Historial de una bicicleta (`GET /api/bicicletas/{codigo}/historial`)**

<img width="600" height="400" alt="image" src="https://github.com/user-attachments/assets/ceab68cf-2e33-479a-b151-9c5d1853ea0f" />

**3. Listar bicicletas (`GET /api/bicicletas`)**

<img width="600" height="400" alt="image" src="https://github.com/user-attachments/assets/ea04315e-74ce-4b07-8b6a-f568e307ee88" />
