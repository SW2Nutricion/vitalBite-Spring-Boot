package com.vitalbite.documental.domain.model;

import java.util.List;

public record MetadatosIA(
        String modelo,
        String version,
        List<String> fuentes) {

    public MetadatosIA {
        fuentes = fuentes == null ? List.of() : List.copyOf(fuentes);
    }

    public boolean completos() {
        return modelo != null && !modelo.isBlank()
                && version != null && !version.isBlank()
                && !fuentes.isEmpty();
    }
}
