package com.vitalbite.documental.application.usecase;

import com.vitalbite.documental.application.dto.DocumentoResponseDTO;
import com.vitalbite.documental.application.port.out.DocumentoRepositoryPort;
import com.vitalbite.documental.domain.model.Documento;

import java.util.UUID;

public final class ConsultarDocumentoUseCase {

    private final DocumentoRepositoryPort repository;

    public ConsultarDocumentoUseCase(DocumentoRepositoryPort repository) {
        this.repository = repository;
    }

    public DocumentoResponseDTO execute(UUID id, String usuarioId, String rol) {
        Documento documento = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        if (!documento.puedeConsultar(usuarioId, rol)) {
            throw new SecurityException("El usuario no puede consultar este documento");
        }
        return new DocumentoResponseDTO(
                documento.id(), documento.tipo(), documento.estado(),
                documento.pacienteId(), documento.origenContenido(),
                documento.formato(), documento.tamanoBytes(),
                documento.hashSha256(), documento.version(),
                documento.creadoEn(), documento.anuladoEn());
    }
}
