package com.vitalbite.documental.infrastructure.out.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.domain.model.AclDocumento;
import com.vitalbite.documental.domain.model.Documento;
import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.domain.model.MetadatosIA;
import com.vitalbite.documental.domain.model.OrigenContenido;
import com.vitalbite.documental.domain.model.TipoDocumento;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@Profile({"local", "test", "production"})
public class JpaDocumentoRepositoryAdapter implements DocumentoRepositoryPort {
    private final SpringDataDocumentoRepository repository;
    private final ObjectMapper objectMapper;

    public JpaDocumentoRepositoryAdapter(SpringDataDocumentoRepository repository,
                                         ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Documento save(Documento documento) {
        repository.save(toEntity(documento));
        return documento;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Documento> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Documento> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private DocumentoEntity toEntity(Documento document) {
        try {
            DocumentoEntity entity = new DocumentoEntity(document.id(), document.tipo().name(),
                    document.estado().name(), document.pacienteId(),
                    document.acl().propietarioId(), document.origenContenido().name(),
                    document.formato(), document.tamanoBytes(), document.hashSha256(),
                    document.version(), document.creadoEn(), document.anuladoEn(),
                    objectMapper.writeValueAsString(document.acl().compartidoCon()),
                    document.metadatosIA() == null ? null
                            : objectMapper.writeValueAsString(document.metadatosIA()),
                    document.tenantId());
            entity.setSagaId(document.sagaId());
            return entity;
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo serializar el documento", exception);
        }
    }

    private Documento toDomain(DocumentoEntity entity) {
        try {
            Set<String> shared = objectMapper.readValue(entity.getCompartidoCon(),
                    new TypeReference<>() {});
            MetadatosIA metadata = entity.getMetadatosIA() == null ? null
                    : objectMapper.readValue(entity.getMetadatosIA(), MetadatosIA.class);
            Documento document = new Documento(entity.getId(),
                    TipoDocumento.valueOf(entity.getTipo()), entity.getPacienteId(),
                    new AclDocumento(entity.getPropietarioId(), entity.getPacienteId(), shared),
                    OrigenContenido.valueOf(entity.getOrigenContenido()), metadata,
                    entity.getFormato(), entity.getTenantId());
            document.asignarSagaId(entity.getSagaId());
            if (entity.getEstado().equals(EstadoDocumento.GENERADO.name())
                    || entity.getEstado().equals(EstadoDocumento.ANULADO.name())) {
                document.marcarGenerado(entity.getTamanoBytes(), entity.getHashSha256());
            } else if (entity.getEstado().equals(EstadoDocumento.FALLIDO.name())) {
                document.marcarFallido();
            }
            if (entity.getEstado().equals(EstadoDocumento.ANULADO.name())) {
                document.anular();
            }
            return document;
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo deserializar el documento", exception);
        }
    }
}
