package com.vitalbite.documental.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditoriaPort {
    void registrar(Registro registro);

    Page buscar(String accion, String resultado, String sub, int page, int size);

    record Registro(String sub, String rol, String accion, UUID documentoId,
                    String resultado, Instant fecha, String correlationId,
                    String sagaId, String ip) {}

    record Entrada(long id, String sub, String rol, String accion, UUID documentoId,
                   String resultado, Instant fecha, String correlationId,
                   String sagaId, String ip) {}

    record Page(List<Entrada> contenido, int page, int size, long totalElements) {}
}
