# VitalBite Spring Boot

Microservicio documental de VitalBite. Genera documentos PDF mediante una
arquitectura hexagonal, separando dominio, aplicación e infraestructura.

La persistencia de metadatos usa PostgreSQL con migraciones Flyway. Los archivos
generados conservan su ruta de almacenamiento y el entorno local incluye MinIO.

## Requisitos

- Java 17
- Maven Wrapper incluido (`mvnw.cmd` en Windows)

## Instalación y ejecución

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

Para levantar PostgreSQL, Keycloak, MinIO y la aplicación:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

El realm `vitalbite` se importa automáticamente desde `realm-export.json` con
los roles, mappers y usuarios de prueba definidos para el entorno local.

El backend se levanta en `http://localhost:8082/api/v1`.

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

Los PDFs se devuelven directamente en la respuesta y no se almacenan todavía.
Los endpoints de consulta, descarga y metadatos se implementarán cuando se
seleccione la tecnología de persistencia.

## Núcleo documental en avance

El endpoint `POST /api/v1/documentos` prueba el contrato documental:

- Requiere `Idempotency-Key`.
- Valida ACL y estados `PENDIENTE`, `GENERADO`, `FALLIDO` y `ANULADO`.
- Genera PDF o CSV a partir de los datos recibidos, sin consultar otros servicios.
- Separa `Datos registrados` de `Interpretación generada por IA`.
- Exige metadatos de IA cuando se envía una interpretación.
- `GET /api/v1/documentos/{id}` devuelve metadatos y `ETag`.
- `POST /api/v1/documentos/{id}/anular` implementa la compensación idempotente.

Con los perfiles `local`, `test` o `production` se usa PostgreSQL; sin perfil
se conserva un adaptador en memoria para pruebas rápidas.

## Pruebas

```powershell
.\mvnw.cmd test
```
