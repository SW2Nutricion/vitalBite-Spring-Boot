package com.vitalbite.documental.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface AlmacenArchivoPort {

    void guardar(UUID documentoId, byte[] contenido);

    Optional<byte[]> leer(UUID documentoId);

    void eliminar(UUID documentoId);
}
