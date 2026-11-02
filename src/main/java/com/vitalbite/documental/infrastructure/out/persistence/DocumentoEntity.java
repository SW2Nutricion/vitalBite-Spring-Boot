package com.vitalbite.documental.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documento")
public class DocumentoEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String tipo;
    @Column(nullable = false)
    private String estado;
    @Column(name = "paciente_id", nullable = false)
    private String pacienteId;
    @Column(name = "propietario_id", nullable = false)
    private String propietarioId;
    @Column(name = "origen_contenido", nullable = false)
    private String origenContenido;
    @Column(nullable = false)
    private String formato;
    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;
    @Column(name = "hash_sha256")
    private String hashSha256;
    @Version
    @Column(nullable = false)
    private long version;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
    @Column(name = "anulado_en")
    private Instant anuladoEn;
    @Column(name = "compartido_con", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String compartidoCon;
    @Column(name = "metadatos_ia")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadatosIA;

    protected DocumentoEntity() {
    }

    public DocumentoEntity(UUID id, String tipo, String estado, String pacienteId,
                           String propietarioId, String origenContenido, String formato,
                           long tamanoBytes, String hashSha256, long version,
                           Instant creadoEn, Instant anuladoEn, String compartidoCon,
                           String metadatosIA) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
        this.pacienteId = pacienteId;
        this.propietarioId = propietarioId;
        this.origenContenido = origenContenido;
        this.formato = formato;
        this.tamanoBytes = tamanoBytes;
        this.hashSha256 = hashSha256;
        this.version = version;
        this.creadoEn = creadoEn;
        this.anuladoEn = anuladoEn;
        this.compartidoCon = compartidoCon;
        this.metadatosIA = metadatosIA;
    }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public String getEstado() { return estado; }
    public String getPacienteId() { return pacienteId; }
    public String getPropietarioId() { return propietarioId; }
    public String getOrigenContenido() { return origenContenido; }
    public String getFormato() { return formato; }
    public long getTamanoBytes() { return tamanoBytes; }
    public String getHashSha256() { return hashSha256; }
    public long getVersion() { return version; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getAnuladoEn() { return anuladoEn; }
    public String getCompartidoCon() { return compartidoCon; }
    public String getMetadatosIA() { return metadatosIA; }
}
