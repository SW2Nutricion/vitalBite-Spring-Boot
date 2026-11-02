package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.port.out.AlmacenArchivoPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!local & !test & !production")
public class InMemoryAlmacenArchivoAdapter implements AlmacenArchivoPort {

    private final ConcurrentHashMap<UUID, byte[]> contenido = new ConcurrentHashMap<>();

    @Override
    public void guardar(UUID documentoId, byte[] bytes) {
        contenido.put(documentoId, Arrays.copyOf(bytes, bytes.length));
    }

    @Override
    public Optional<byte[]> leer(UUID documentoId) {
        byte[] bytes = contenido.get(documentoId);
        return bytes == null ? Optional.empty()
                : Optional.of(Arrays.copyOf(bytes, bytes.length));
    }

    @Override
    public void eliminar(UUID documentoId) {
        contenido.remove(documentoId);
    }
}
