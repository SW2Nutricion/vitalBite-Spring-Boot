package com.vitalbite.documental.infrastructure.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "idempotencia")
public class IdempotenciaEntity {
    @Id
    private String clave;
    @Column(name = "hash_solicitud", nullable = false)
    private String hashSolicitud;
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String respuesta;
    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected IdempotenciaEntity() {
    }

    public IdempotenciaEntity(String clave, String hashSolicitud, String respuesta,
                              Instant creadoEn) {
        this.clave = clave;
        this.hashSolicitud = hashSolicitud;
        this.respuesta = respuesta;
        this.creadoEn = creadoEn;
    }

    public String getClave() { return clave; }
    public String getHashSolicitud() { return hashSolicitud; }
    public String getRespuesta() { return respuesta; }
    public Instant getCreadoEn() { return creadoEn; }
}
