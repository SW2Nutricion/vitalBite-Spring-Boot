package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.IdempotenciaPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!local & !test & !production")
public class InMemoryIdempotenciaAdapter implements IdempotenciaPort {
    private final Map<String, RegistroIdempotencia> values = new ConcurrentHashMap<>();

    @Override
    public Optional<RegistroIdempotencia> find(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public Optional<RegistroIdempotencia> reserve(String key, String requestHash) {
        return Optional.ofNullable(values.putIfAbsent(key,
                new RegistroIdempotencia(requestHash, null)));
    }

    @Override
    public void save(String key, String requestHash, DocumentoResponseDTO response) {
        values.putIfAbsent(key, new RegistroIdempotencia(requestHash, response));
    }

    @Override
    public void purgeExpired() {
    }
}
