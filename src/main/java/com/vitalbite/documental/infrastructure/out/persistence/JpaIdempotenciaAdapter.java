package com.vitalbite.documental.infrastructure.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.IdempotenciaPort;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
@Profile({"local", "test", "production"})
public class JpaIdempotenciaAdapter implements IdempotenciaPort {
    private static final Duration RETENTION = Duration.ofHours(24);
    private final SpringDataIdempotenciaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaIdempotenciaAdapter(SpringDataIdempotenciaRepository repository,
                                   ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroIdempotencia> find(String key) {
        return repository.findById(key).map(entity -> {
            try {
                return new RegistroIdempotencia(entity.getHashSolicitud(),
                        entity.getRespuesta() == null ? null : objectMapper.readValue(
                                entity.getRespuesta(), DocumentoResponseDTO.class));
            } catch (Exception exception) {
                throw new IllegalStateException(
                        "No se pudo leer la respuesta de idempotencia", exception);
            }
        });
    }

    @Override
    @Transactional
    public Optional<RegistroIdempotencia> reserve(String key, String requestHash) {
        if (repository.insertReservation(key, requestHash) == 1) {
            return Optional.empty();
        }
        return find(key);
    }

    @Override
    @Transactional
    public void save(String key, String requestHash, DocumentoResponseDTO response) {
        try {
            repository.saveAndFlush(new IdempotenciaEntity(key, requestHash,
                    objectMapper.writeValueAsString(response), Instant.now()));
        } catch (DataIntegrityViolationException exception) {
            RegistroIdempotencia existing = find(key).orElseThrow(() -> exception);
            if (!existing.requestHash().equals(requestHash)) {
                throw new IllegalStateException(
                        "La clave de idempotencia ya fue usada con otro cuerpo");
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudo guardar la idempotencia", exception);
        }
    }

    @Override
    @Transactional
    public void purgeExpired() {
        repository.deleteExpired(Instant.now().minus(RETENTION));
    }
}
