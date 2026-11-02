package com.vitalbite.documental.infrastructure.out.persistence;

import com.vitalbite.documental.application.port.out.AuditoriaPort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile({"local", "test", "production"})
public class JpaAuditoriaAdapter implements AuditoriaPort {
    private final SpringDataAuditoriaRepository repository;

    public JpaAuditoriaAdapter(SpringDataAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void registrar(Registro registro) {
        repository.save(new AuditoriaEntity(registro.sub(), registro.rol(), registro.accion(),
                registro.documentoId(), registro.resultado(), registro.fecha(),
                registro.correlationId(), registro.sagaId(), registro.ip()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page buscar(String accion, String resultado, String sub, int page, int size) {
        var result = repository.search(accion, resultado, sub, PageRequest.of(page, size));
        return new Page(result.getContent().stream()
                .map(a -> new Entrada(a.getId(), a.getSub(), a.getRol(), a.getAccion(),
                        a.getDocumentoId(), a.getResultado(), a.getFecha(),
                        a.getCorrelationId(), a.getSagaId(), a.getIp())).toList(),
                page, size, result.getTotalElements());
    }
}
