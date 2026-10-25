package com.vitalbite.documental.application.dto;

import java.util.List;

public record MetadatosIADTO(
        String modelo,
        String version,
        List<String> fuentes) {
}
