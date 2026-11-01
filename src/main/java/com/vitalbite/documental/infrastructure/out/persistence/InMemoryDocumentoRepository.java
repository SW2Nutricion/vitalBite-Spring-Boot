package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.domain.model.Documento;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryDocumentoRepository implements DocumentoRepositoryPort {

    private final ConcurrentHashMap<UUID, Documento> documentos = new ConcurrentHashMap<>();

    @Override
    public Documento save(Documento documento) {
        documentos.put(documento.id(), documento);
        return documento;
    }

    @Override
    public Optional<Documento> findById(UUID id) {
        return Optional.ofNullable(documentos.get(id));
    }

    @Override
    public List<Documento> findAll() {
        return List.copyOf(documentos.values());
    }
}
