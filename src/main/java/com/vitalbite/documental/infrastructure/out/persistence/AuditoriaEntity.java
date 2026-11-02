package com.vitalbite.documental.infrastructure.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria")
public class AuditoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sub;
    private String rol;
    @Column(nullable = false)
    private String accion;
    @Column(name = "documento_id")
    private UUID documentoId;
    @Column(nullable = false)
    private String resultado;
    @Column(nullable = false)
    private Instant fecha;
    @Column(name = "correlation_id")
    private String correlationId;
    @Column(name = "saga_id")
    private String sagaId;
    private String ip;

    protected AuditoriaEntity() {}

    public AuditoriaEntity(String sub, String rol, String accion, UUID documentoId,
                           String resultado, Instant fecha, String correlationId,
                           String sagaId, String ip) {
        this.sub = sub; this.rol = rol; this.accion = accion; this.documentoId = documentoId;
        this.resultado = resultado; this.fecha = fecha; this.correlationId = correlationId;
        this.sagaId = sagaId; this.ip = ip;
    }

    public Long getId() { return id; }
    public String getSub() { return sub; }
    public String getRol() { return rol; }
    public String getAccion() { return accion; }
    public UUID getDocumentoId() { return documentoId; }
    public String getResultado() { return resultado; }
    public Instant getFecha() { return fecha; }
    public String getCorrelationId() { return correlationId; }
    public String getSagaId() { return sagaId; }
    public String getIp() { return ip; }
}
