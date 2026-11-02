### Fase A — Base de infraestructura
 
#### T01 · Entorno local con Docker Compose y perfiles
- **Depende de:** T00
- **Objetivo:** levantar todo el entorno con un comando.
- **Hacer:**
  - Ampliar `docker-compose.yml` con PostgreSQL, Keycloak y MinIO (el servicio actual solo tiene la app).
  - Perfiles `local`, `test`, `production` en `application-*.properties`.
  - Actualizar `.env.example` con las variables nuevas (BD, issuer del IdP, MinIO).
  - **Spike (1 hora):** crear en Keycloak un usuario con **id propio** (el UUID del core) y confirmar que `sub` lo respeta (decisión D4). Si no, usar el claim `core_id` y avisar.
  - Versionar un `realm-export.json` con los clientes, roles (`ADMIN`, `NUTRICIONISTA`, `PACIENTE`, `SERVICE`), el mapper de audiencia, el mapper de `tenant_id` y usuarios de prueba (un admin, un nutricionista, dos pacientes de dos clínicas distintas).
- **Terminado cuando:** `docker compose up` levanta los 4 contenedores, el realm se importa solo y la app arranca con el perfil `local`.
#### T02 · Esquema PostgreSQL con Flyway
- **Depende de:** T01
- **Objetivo:** esquema versionado.
- **Hacer:** agregar dependencias `spring-boot-starter-data-jpa`, `postgresql`, `flyway`. Migraciones para:
  - `documento` (id, tipo, estado, paciente_id, propietario_id, origen_contenido, formato, tamano_bytes, hash_sha256, version, creado_en, anulado_en, ruta_almacen, tenant_id)
  - `documento_compartido` (documento_id, usuario_id)
  - `documento_metadatos_ia` (documento_id, modelo, version, fuentes)
  - `idempotencia` (clave, hash_solicitud, respuesta, creado_en) con clave única
  - `auditoria` (ver T09)
- **Terminado cuando:** las migraciones corren en BD vacía y hay un test que lo verifica.
#### T03 · Adaptador de persistencia (JPA) para `DocumentoRepositoryPort`
- **Depende de:** T02
- **Objetivo:** reemplazar `InMemoryDocumentoRepository`.
- **Hacer:**
  - Entidades JPA y mapeador **dentro de `infrastructure/out/persistence`**; no tocar `Documento` del dominio.
  - Control de concurrencia optimista con la columna `version`.
  - Agregar a `DocumentoRepositoryPort` lo que haga falta (por ejemplo `buscar(filtros, página)`), sin romper los casos de uso existentes.
- **Terminado cuando:** un documento sobrevive a reiniciar la app y el `ArchitectureTest` sigue verde.
#### T04 · Idempotencia persistente
- **Depende de:** T03
- **Objetivo:** que `Idempotency-Key` sirva de verdad entre reinicios y réplicas.
- **Hacer:**
  - Puerto `IdempotenciaPort` en `application`; adaptador en BD con restricción única (elimina la condición de carrera de `get` + `putIfAbsent`).
  - Hash **canónico** del cuerpo (JSON ordenado), no `toString()`.
  - Aplicar también a `anular`, que hoy exige la clave pero no la guarda.
  - Política de expiración de claves.
- **Terminado cuando:** mismo cuerpo + misma clave devuelve la misma respuesta; distinto cuerpo da 409; dos peticiones concurrentes crean un solo documento (test).
