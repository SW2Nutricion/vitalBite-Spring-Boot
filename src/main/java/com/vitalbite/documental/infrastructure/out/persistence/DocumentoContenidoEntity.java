package com.vitalbite.documental.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documento_contenido")
public class DocumentoContenidoEntity {

    @Id
    @Column(name = "documento_id")
    private UUID documentoId;

    @Column(nullable = false)
    private byte[] contenido;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected DocumentoContenidoEntity() {
    }

    public DocumentoContenidoEntity(UUID documentoId, byte[] contenido, Instant creadoEn) {
        this.documentoId = documentoId;
        this.contenido = contenido.clone();
        this.creadoEn = creadoEn;
    }

    public byte[] getContenido() {
        return contenido.clone();
    }
}
