package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.port.out.AuditoriaPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("!local & !test & !production")
public class InMemoryAuditoriaAdapter implements AuditoriaPort {
    private final AtomicLong ids = new AtomicLong();
    private final ArrayList<Entrada> entries = new ArrayList<>();

    @Override
    public synchronized void registrar(Registro r) {
        entries.add(new Entrada(ids.incrementAndGet(), r.sub(), r.rol(), r.accion(),
                r.documentoId(), r.resultado(), r.fecha(), r.correlationId(), r.sagaId(), r.ip()));
    }

    @Override
    public synchronized Page buscar(String accion, String resultado, String sub, int page, int size) {
        var filtered = entries.stream().filter(e -> accion == null || accion.equals(e.accion()))
                .filter(e -> resultado == null || resultado.equals(e.resultado()))
                .filter(e -> sub == null || sub.equals(e.sub()))
                .sorted(Comparator.comparing(Entrada::fecha).reversed()).toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return new Page(filtered.subList(from, to), page, size, filtered.size());
    }
}
