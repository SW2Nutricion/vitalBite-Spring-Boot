package com.vitalbite.documental.domain.model;

import com.vitalbite.documental.domain.exception.TransicionDeDocumentoInvalida;

import java.time.Instant;
import java.util.UUID;

public final class Documento {

    private final UUID id;
    private final TipoDocumento tipo;
    private final String pacienteId;
    private final AclDocumento acl;
    private final OrigenContenido origenContenido;
    private final MetadatosIA metadatosIA;
    private final String formato;
    private final Instant creadoEn;
    private EstadoDocumento estado;
    private long tamanoBytes;
    private String hashSha256;
    private long version;
    private Instant anuladoEn;

    public Documento(UUID id, TipoDocumento tipo, String pacienteId,
                     AclDocumento acl, OrigenContenido origenContenido,
                     MetadatosIA metadatosIA, String formato) {
        if (origenContenido != OrigenContenido.REGISTRADO
                && (metadatosIA == null || !metadatosIA.completos())) {
            throw new IllegalArgumentException(
                    "metadatosIA completos son obligatorios para contenido de IA");
        }
        this.id = id == null ? UUID.randomUUID() : id;
        this.tipo = tipo;
        this.pacienteId = pacienteId;
        this.acl = acl;
        this.origenContenido = origenContenido;
        this.metadatosIA = metadatosIA;
        this.formato = formato;
        this.estado = EstadoDocumento.PENDIENTE;
        this.creadoEn = Instant.now();
    }

    public void marcarGenerado(long tamanoBytes, String hashSha256) {
        transicionar(EstadoDocumento.GENERADO);
        this.tamanoBytes = tamanoBytes;
        this.hashSha256 = hashSha256;
    }

    public void marcarFallido() {
        transicionar(EstadoDocumento.FALLIDO);
    }

    public void anular() {
        if (estado == EstadoDocumento.ANULADO) {
            return;
        }
        transicionar(EstadoDocumento.ANULADO);
        anuladoEn = Instant.now();
    }

    public boolean puedeConsultar(String usuarioId, String rol) {
        return acl.puedeConsultar(usuarioId, rol);
    }

    private void transicionar(EstadoDocumento nuevoEstado) {
        boolean valida = estado == EstadoDocumento.PENDIENTE
                && (nuevoEstado == EstadoDocumento.GENERADO
                || nuevoEstado == EstadoDocumento.FALLIDO
                || nuevoEstado == EstadoDocumento.ANULADO)
                || estado == EstadoDocumento.GENERADO
                && nuevoEstado == EstadoDocumento.ANULADO;
        if (!valida) {
            throw new TransicionDeDocumentoInvalida(
                    "Transición inválida: " + estado + " -> " + nuevoEstado);
        }
        estado = nuevoEstado;
        version++;
    }

    public UUID id() { return id; }
    public TipoDocumento tipo() { return tipo; }
    public String pacienteId() { return pacienteId; }
    public AclDocumento acl() { return acl; }
    public OrigenContenido origenContenido() { return origenContenido; }
    public MetadatosIA metadatosIA() { return metadatosIA; }
    public String formato() { return formato; }
    public Instant creadoEn() { return creadoEn; }
    public EstadoDocumento estado() { return estado; }
    public long tamanoBytes() { return tamanoBytes; }
    public String hashSha256() { return hashSha256; }
    public long version() { return version; }
    public Instant anuladoEn() { return anuladoEn; }
}
