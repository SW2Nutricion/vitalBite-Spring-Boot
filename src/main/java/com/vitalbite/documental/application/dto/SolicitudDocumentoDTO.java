package com.vitalbite.documental.application.dto;

import com.vitalbite.documental.domain.model.OrigenContenido;
import com.vitalbite.documental.domain.model.TipoDocumento;

import java.util.Map;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SolicitudDocumentoDTO(
        @NotNull
        TipoDocumento tipo,
        String pacienteId,
        @NotBlank
        String propietarioId,
        Set<String> compartidoCon,
        @NotNull
        OrigenContenido origenContenido,
        @NotEmpty
        Map<String, Object> datos,
        String interpretacionIA,
        MetadatosIADTO metadatosIA,
        @NotBlank
        String formato,
        String tenantId) {
}
