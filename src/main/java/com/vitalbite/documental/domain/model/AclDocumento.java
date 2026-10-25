package com.vitalbite.documental.domain.model;

import java.util.Set;

public record AclDocumento(
        String propietarioId,
        String pacienteId,
        Set<String> compartidoCon) {

    public AclDocumento {
        compartidoCon = compartidoCon == null ? Set.of() : Set.copyOf(compartidoCon);
    }

    public boolean puedeConsultar(String usuarioId, String rol) {
        if ("ADMIN".equals(rol)) {
            return true;
        }
        if ("PACIENTE".equals(rol)) {
            return pacienteId.equals(usuarioId);
        }
        return propietarioId.equals(usuarioId) || compartidoCon.contains(usuarioId);
    }
}
