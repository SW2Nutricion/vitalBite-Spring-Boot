package com.vitalbite.documental.application.dto;

import com.vitalbite.documental.domain.model.OrigenContenido;
import com.vitalbite.documental.domain.model.TipoDocumento;

import java.util.Map;
import java.util.Set;

public record SolicitudDocumentoDTO(
        TipoDocumento tipo,
        String pacienteId,
        String propietarioId,
        Set<String> compartidoCon,
        OrigenContenido origenContenido,
        Map<String, Object> datos,
        String interpretacionIA,
        MetadatosIADTO metadatosIA,
        String formato) {
}
