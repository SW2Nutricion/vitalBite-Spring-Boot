# VitalBite Spring Boot

Microservicio documental de VitalBite. Genera documentos PDF mediante una
arquitectura hexagonal, separando dominio, aplicación e infraestructura.

La persistencia de metadatos y contenido usa PostgreSQL con migraciones Flyway.
El contenido se almacena en una tabla separada como `BYTEA`.

## Requisitos

- Java 17
- Maven Wrapper incluido (`mvnw.cmd` en Windows)

## Instalación y ejecución

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

Para levantar PostgreSQL, Keycloak y la aplicación:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

El realm `vitalbite` se importa automáticamente desde `realm-export.json` con
los roles, mappers y usuarios de prueba definidos para el entorno local.

El backend se levanta en `http://localhost:8082/api/v1`.

Los endpoints protegidos requieren un JWT emitido por el IdP configurado en
`SSO_ISSUER_URI`. El token debe incluir la audiencia `vitalbite-documental`;
los roles `NUTRICIONISTA` y `PACIENTE` también requieren `tenant_id`.

## URLs útiles

- Swagger UI: `http://localhost:8082/api/v1/swagger-ui/index.html`
- Documentación OpenAPI: `http://localhost:8082/api/v1/v3/api-docs`
- Actuator health: `http://localhost:8082/api/v1/actuator/health`

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/documents/pdf/diet` | Genera y devuelve un PDF de dieta |
| `POST` | `/api/v1/documents/pdf/diet/preview` | Genera una vista previa de dieta |
| `POST` | `/api/v1/documents/pdf/invoice` | Genera y devuelve un PDF de factura |

Los endpoints `/documents/pdf/*` se conservan de forma compatible. El flujo
principal `/documentos` almacena el contenido y permite descargarlo desde
`GET /documentos/{id}/contenido`.

## Núcleo documental en avance

El endpoint `POST /api/v1/documentos` prueba el contrato documental:

- Requiere `Idempotency-Key`.
- Valida ACL y estados `PENDIENTE`, `GENERADO`, `FALLIDO` y `ANULADO`.
- Genera PDF o CSV a partir de los datos recibidos, sin consultar otros servicios.
- Separa `Datos registrados` de `Interpretación generada por IA`.
- Exige metadatos de IA cuando se envía una interpretación.
- `GET /api/v1/documentos/{id}` devuelve metadatos y `ETag`.
- `POST /api/v1/documentos/{id}/anular` implementa la compensación idempotente.
- `GET /api/v1/documentos/{id}/contenido` descarga el contenido verificando
  SHA-256 y ACL.
- `GET /api/v1/documentos` lista documentos con filtros y paginación.

Con los perfiles `local` o `production` se usa PostgreSQL; el perfil `test`
usa H2 en memoria para no depender de servicios externos. Sin perfil se
conserva un adaptador en memoria para pruebas rápidas.

## Pruebas

```powershell
.\mvnw.cmd test
```
## Persistencia y auditoría

El servicio persiste documentos, contenido, idempotencia y una bitácora
append-only en PostgreSQL. `GET /api/v1/auditoria` requiere el rol `ADMIN` y
acepta filtros `accion`, `resultado`, `sub`, `page` y `size`.

Las operaciones distribuidas aceptan `X-Saga-Id`; la coordinación HTTP y la
política de reintentos están documentadas en [docs/saga.md](docs/saga.md).
