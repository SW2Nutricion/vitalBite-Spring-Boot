package com.vitalbite.documental.application.port.out;

import com.vitalbite.documental.domain.model.Documento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoRepositoryPort {

    Documento save(Documento documento);

    Optional<Documento> findById(UUID id);

    List<Documento> findAll();
}
