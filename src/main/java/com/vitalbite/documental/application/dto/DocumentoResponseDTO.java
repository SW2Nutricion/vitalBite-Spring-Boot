package com.vitalbite.documental.application.dto;

import com.vitalbite.documental.domain.model.EstadoDocumento;
import com.vitalbite.documental.domain.model.OrigenContenido;
import com.vitalbite.documental.domain.model.TipoDocumento;

import java.time.Instant;
import java.util.UUID;

public record DocumentoResponseDTO(
        UUID id,
        TipoDocumento tipo,
        EstadoDocumento estado,
        String pacienteId,
        OrigenContenido origenContenido,
        String formato,
        long tamanoBytes,
        String hashSha256,
        long version,
        Instant creadoEn,
        Instant anuladoEn) {
}
