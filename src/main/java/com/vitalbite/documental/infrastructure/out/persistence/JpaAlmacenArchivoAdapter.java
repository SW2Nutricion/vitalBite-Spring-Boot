package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.port.out.AlmacenArchivoPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile({"local", "test", "production"})
public class JpaAlmacenArchivoAdapter implements AlmacenArchivoPort {

    private final SpringDataDocumentoContenidoRepository repository;

    public JpaAlmacenArchivoAdapter(SpringDataDocumentoContenidoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void guardar(UUID documentoId, byte[] contenido) {
        repository.save(new DocumentoContenidoEntity(documentoId, contenido, Instant.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<byte[]> leer(UUID documentoId) {
        return repository.findById(documentoId).map(DocumentoContenidoEntity::getContenido);
    }

    @Override
    @Transactional
    public void eliminar(UUID documentoId) {
        repository.deleteById(documentoId);
    }
}
