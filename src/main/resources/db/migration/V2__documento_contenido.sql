CREATE TABLE documento_contenido (
    documento_id UUID PRIMARY KEY REFERENCES documento(id) ON DELETE CASCADE,
    contenido BYTEA NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
