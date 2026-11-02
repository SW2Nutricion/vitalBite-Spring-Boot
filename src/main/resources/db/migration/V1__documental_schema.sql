CREATE TABLE documento (
    id UUID PRIMARY KEY,
    tipo VARCHAR(64) NOT NULL,
    estado VARCHAR(32) NOT NULL,
    paciente_id VARCHAR(255),
    propietario_id VARCHAR(255) NOT NULL,
    origen_contenido VARCHAR(32) NOT NULL,
    formato VARCHAR(64) NOT NULL,
    tamano_bytes BIGINT NOT NULL DEFAULT 0,
    hash_sha256 VARCHAR(128),
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMPTZ NOT NULL,
    anulado_en TIMESTAMPTZ,
    ruta_almacen VARCHAR(1024),
    tenant_id VARCHAR(255),
    compartido_con JSONB NOT NULL DEFAULT '[]'::jsonb,
    metadatos_ia JSONB
);

CREATE TABLE documento_compartido (
    documento_id UUID NOT NULL REFERENCES documento(id) ON DELETE CASCADE,
    usuario_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (documento_id, usuario_id)
);

CREATE TABLE documento_metadatos_ia (
    documento_id UUID PRIMARY KEY REFERENCES documento(id) ON DELETE CASCADE,
    modelo VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    fuentes JSONB NOT NULL
);

CREATE TABLE idempotencia (
    clave VARCHAR(255) PRIMARY KEY,
    hash_solicitud VARCHAR(128) NOT NULL,
    respuesta JSONB NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255),
    accion VARCHAR(128) NOT NULL,
    recurso_id VARCHAR(255),
    detalles JSONB,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255)
);

CREATE INDEX idx_documento_paciente ON documento (paciente_id);
CREATE INDEX idx_idempotencia_expiracion ON idempotencia (creado_en);
