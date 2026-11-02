package com.vitalbite.documental.application.port.out;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;

import java.util.Optional;

public interface IdempotenciaPort {
    Optional<RegistroIdempotencia> find(String key);
    Optional<RegistroIdempotencia> reserve(String key, String requestHash);
    void save(String key, String requestHash, DocumentoResponseDTO response);
    void purgeExpired();

    record RegistroIdempotencia(String requestHash, DocumentoResponseDTO response) {
    }
}
