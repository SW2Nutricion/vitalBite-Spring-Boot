# Diagnóstico y mejoras del microservicio VitalBite Documental

## Resumen

El proyecto se consolidó como un microservicio Spring Boot dedicado a la
generación y gestión documental de VitalBite. Su responsabilidad principal es
recibir información clínica o administrativa, generar documentos en PDF o CSV
y administrar sus metadatos, estado, control de acceso y anulación.

La mejora arquitectónica principal fue migrar el microservicio a una
**arquitectura hexagonal (Ports and Adapters)**. Con este cambio, la lógica de
negocio deja de depender directamente de Spring, de la persistencia o de la
tecnología utilizada para generar los archivos.

## Mejoras realizadas

### 1. Migración a arquitectura hexagonal

El código se separó en tres zonas:

```text
com.vitalbite.documental
├── domain
│   ├── model
│   └── exception
├── application
│   ├── dto
│   ├── exception
│   ├── port
│   │   └── out
│   └── usecase
└── infrastructure
    ├── config
    ├── in
    │   └── web
    └── out
        ├── generator
        ├── pdf
        └── persistence
```

- **Dominio:** contiene las reglas que no dependen de Spring. Incluye
  `Documento`, `AclDocumento`, los estados del documento, los tipos de
  documento, el origen del contenido y las reglas de transición.
- **Aplicación:** contiene los casos de uso, DTOs y puertos de salida. Aquí se
  coordina el flujo de generación, consulta y anulación sin conocer los
  detalles de HTTP, iText o la base de datos.
- **Infraestructura:** contiene los adaptadores concretos. Actualmente incluye
  los controladores REST, el generador PDF/CSV, el repositorio temporal en
  memoria, la configuración Spring y el manejo de errores.

Los casos de uso dependen de interfaces, no de implementaciones concretas:

- `DocumentoRepositoryPort`: persistencia de metadatos documentales.
- `GeneradorDocumentoPort`: generación de documentos desde una solicitud.
- `PdfGeneratorPort`: generación especializada de PDFs de dietas y facturas.

Esto permite reemplazar el repositorio en memoria por una base de datos o
reemplazar el generador sin modificar el dominio ni los casos de uso.

### 2. Incorporación del núcleo de gestión documental

Además de la generación directa de PDFs, el proyecto ahora administra el
ciclo de vida de un documento:

- Estados `PENDIENTE`, `GENERADO`, `FALLIDO` y `ANULADO`.
- Validación de transiciones de estado dentro del dominio.
- Identificador único `UUID`.
- Tamaño y hash SHA-256 del archivo generado.
- Versión del documento para control mediante `ETag`.
- Fecha de creación y fecha de anulación.
- Anulación como compensación de una Saga.

### 3. Idempotencia

Las operaciones de solicitud y anulación requieren el header
`Idempotency-Key`. Una misma clave con el mismo cuerpo devuelve el resultado
previo; si se reutiliza con otro cuerpo, la operación se rechaza.

La implementación actual mantiene las claves en memoria, por lo que este
comportamiento deberá trasladarse a una solución persistente cuando se agregue
la base de datos.

### 4. Control de acceso y separación del contenido

El dominio incorpora una ACL por documento con:

- Propietario.
- Paciente.
- Usuarios con los que se compartió el documento.
- Rol `ADMIN`.

La comprobación de esta ACL se realiza dentro de los casos de uso. Los headers
`X-User-Id` y `X-User-Role` representan actualmente la identidad recibida por
el microservicio; todavía no existe integración con un proveedor de
autenticación. Por eso, la configuración de Spring Security mantiene las
rutas abiertas durante esta etapa de desarrollo y no debe considerarse una
solución de autenticación para producción.

También se separan explícitamente dos secciones en el contenido generado:

- **Datos registrados**, provenientes de la solicitud.
- **Interpretación generada por IA**, acompañada por la advertencia de que no
  sustituye la atención profesional.

Cuando el contenido incluye interpretación de IA, se exigen metadatos de
trazabilidad:

- Modelo utilizado.
- Versión del modelo.
- Fuentes consultadas.

### 5. Manejo uniforme de errores

El proyecto incorpora un `GlobalExceptionHandler` que transforma errores de
aplicación y dominio en respuestas `ProblemDetail`. Se contemplan, entre
otros, los siguientes casos:

- Solicitud inválida: `400 Bad Request`.
- Operación incompatible con el estado actual: `409 Conflict`.
- Acceso no permitido: `403 Forbidden`.
- Contenido de IA incompleto: `422 Unprocessable Entity`.
- Archivo que supera el límite configurado: `413 Payload Too Large`.
- Error no controlado: `500 Internal Server Error`.

### 6. Observabilidad y documentación de la API

Se agregaron o configuraron:

- `X-Correlation-Id` para rastrear una solicitud de extremo a extremo.
- Registro de logs con nivel configurable mediante `LOG_LEVEL`.
- Spring Boot Actuator para health checks.
- Swagger UI y documentación OpenAPI.
- Headers `ETag` en las respuestas de consulta, creación y anulación.
- Redirección de la ruta raíz hacia Swagger UI.

### 7. Pruebas y protección de la arquitectura

El proyecto incorpora pruebas de:

- Carga del contexto Spring Boot.
- Reglas del dominio documental.
- Reglas de arquitectura con ArchUnit.

La prueba arquitectónica verifica que las capas `domain` y `application` no
dependan de `infrastructure`, Spring ni JPA. Esto evita que la arquitectura
hexagonal se deteriore con nuevos cambios.

## Endpoints disponibles

La aplicación se ejecuta por defecto en:

```text
http://localhost:8082/api/v1
```

### Generación de PDFs

| Método | Endpoint | Respuesta | Descripción |
|---|---|---|---|
| `POST` | `/documents/pdf/diet` | `application/pdf` | Genera un PDF de dieta. |
| `POST` | `/documents/pdf/diet/preview` | `application/pdf` | Genera una vista previa de una dieta. |
| `POST` | `/documents/pdf/invoice` | `application/pdf` | Genera un PDF de factura. |

Los archivos se devuelven directamente en la respuesta con disposición
`inline`. Estos endpoints utilizan el adaptador PDF basado en iText.

### Gestión del ciclo de vida documental

| Método | Endpoint | Headers relevantes | Descripción |
|---|---|---|---|
| `POST` | `/documentos` | `Idempotency-Key` obligatorio | Genera y registra un documento documental. |
| `GET` | `/documentos/{id}` | `X-User-Id`, `X-User-Role` | Consulta metadatos y estado aplicando la ACL. |
| `POST` | `/documentos/{id}/anular` | `X-User-Id`, `X-User-Role`, `Idempotency-Key` | Anula un documento como compensación de Saga. |

Las respuestas de gestión documental incluyen un `ETag` basado en la versión
del documento.

#### Campos principales de `POST /documentos`

El cuerpo acepta:

- `tipo`: `REPORTE_EVOLUCION`, `REPORTE_CONSULTAS`,
  `PLAN_NUTRICIONAL` o `EXPORTACION_CSV`.
- `pacienteId`: identificador del paciente.
- `propietarioId`: identificador del propietario del documento.
- `compartidoCon`: conjunto opcional de usuarios autorizados.
- `origenContenido`: `REGISTRADO`, `GENERADO_IA` o `MIXTO`.
- `datos`: mapa con los datos que se documentarán.
- `interpretacionIA`: interpretación opcional generada por IA.
- `metadatosIA`: modelo, versión y fuentes; obligatorio cuando se envía
  `interpretacionIA`.
- `formato`: formato de salida; `CSV` genera CSV y cualquier otro formato
  soportado por el adaptador actual genera PDF.

Ejemplo de solicitud:

```json
{
  "tipo": "PLAN_NUTRICIONAL",
  "pacienteId": "paciente-123",
  "propietarioId": "nutricionista-456",
  "compartidoCon": ["usuario-789"],
  "origenContenido": "MIXTO",
  "datos": {
    "caloriasDiarias": 2200,
    "objetivo": "Mantenimiento"
  },
  "interpretacionIA": "Se recomienda distribuir las calorías en cinco comidas.",
  "metadatosIA": {
    "modelo": "modelo-nutricional",
    "version": "1.0",
    "fuentes": ["historial-clinico", "evaluacion-nutricional"]
  },
  "formato": "PDF"
}
```

### Operación y documentación

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/` | Redirige a Swagger UI. |
| `GET` | `/swagger-ui/index.html` | Interfaz interactiva de la API. |
| `GET` | `/v3/api-docs` | Especificación OpenAPI. |
| `GET` | `/actuator/health` | Estado de salud del microservicio. |

## Nuevas responsabilidades del proyecto Spring Boot

Actualmente Spring Boot se encarga de:

1. Levantar y configurar el microservicio web en el puerto `8082`.
2. Publicar todos los endpoints REST bajo el contexto `/api/v1`.
3. Resolver la inyección de dependencias entre casos de uso, puertos y
   adaptadores.
4. Convertir solicitudes JSON en DTOs y respuestas de los casos de uso en
   respuestas HTTP.
5. Negociar respuestas PDF mediante `application/pdf`.
6. Publicar documentación OpenAPI y Swagger UI.
7. Aplicar la cadena de seguridad HTTP configurada para la etapa actual y
   exponer los endpoints públicos del microservicio.
8. Generar y propagar identificadores de correlación para las solicitudes.
9. Convertir excepciones de dominio y aplicación en respuestas HTTP uniformes.
10. Exponer health checks mediante Actuator.
11. Configurar logs y variables de entorno, incluido `LOG_LEVEL`.
12. Registrar los adaptadores temporales de generación y persistencia.

La lógica de negocio no queda delegada al controlador: los controladores solo
traducen HTTP y llaman a los casos de uso. Las reglas de estados, ACL,
idempotencia, validación de metadatos de IA y cálculo de hashes se mantienen
en las capas de aplicación y dominio.

## Estado actual y pendientes

La persistencia de metadatos todavía utiliza
`InMemoryDocumentoRepository`. Por lo tanto:

- Los documentos y claves de idempotencia se pierden al reiniciar la
  aplicación.
- El archivo generado no se almacena todavía para una descarga posterior.
- No existe aún una base de datos, driver, migración ni almacenamiento de
  objetos configurado.
- Los endpoints de consulta implementados devuelven metadatos y estado, no el
  binario del documento.

El siguiente paso de evolución consiste en implementar adaptadores persistentes
para `DocumentoRepositoryPort` y para el almacenamiento de archivos, sin
modificar el dominio ni los casos de uso.

## Tecnologías principales

- Java 17.
- Spring Boot 4.0.6.
- Spring Web.
- Spring Security.
- Spring Validation.
- Spring Boot Actuator.
- SpringDoc OpenAPI.
- iText 8 para generación de PDF.
- Lombok.
- ArchUnit y Spring Boot Test.

## Ejecución y pruebas

En Windows:

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```
## NOTA : todavia no tiene una base de datos fija se debe definir si mongo , dinamo o posgresql