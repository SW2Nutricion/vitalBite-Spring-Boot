package com.vitalbite.documental.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataDocumentoContenidoRepository
        extends JpaRepository<DocumentoContenidoEntity, UUID> {
}
